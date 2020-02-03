package com.vikingsen.inject.fragment.processor

import com.google.auto.service.AutoService
import com.squareup.javapoet.JavaFile
import com.vikingsen.inject.fragment.FragmentInject
import com.vikingsen.inject.fragment.FragmentModule
import com.vikingsen.inject.fragment.processor.internal.MirrorValue
import com.vikingsen.inject.fragment.processor.internal.applyEach
import com.vikingsen.inject.fragment.processor.internal.asDependencyRequest
import com.vikingsen.inject.fragment.processor.internal.cast
import com.vikingsen.inject.fragment.processor.internal.castEach
import com.vikingsen.inject.fragment.processor.internal.createGeneratedAnnotation
import com.vikingsen.inject.fragment.processor.internal.findElementsAnnotatedWith
import com.vikingsen.inject.fragment.processor.internal.getAnnotation
import com.vikingsen.inject.fragment.processor.internal.getValue
import com.vikingsen.inject.fragment.processor.internal.hasAnnotation
import com.vikingsen.inject.fragment.processor.internal.toClassName
import com.vikingsen.inject.fragment.processor.internal.toTypeName
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
@AutoService(Processor::class)
class FragmentInjectProcessor : AbstractProcessor() {

    private lateinit var messager: Messager
    private lateinit var filer: Filer
    private lateinit var types: Types
    private lateinit var elements: Elements
    private lateinit var fragmentType: TypeMirror

    private var userModule: String? = null

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()
    override fun getSupportedAnnotationTypes() = setOf(
        FragmentInject::class.java.canonicalName,
        FragmentModule::class.java.canonicalName
    )

    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        messager = env.messager
        filer = env.filer
        types = env.typeUtils
        elements = env.elementUtils
        fragmentType = elements.getTypeElement("androidx.fragment.app.Fragment").asType()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val fragmentInjectElements = roundEnv.findFragmentInjectCandidateTypeElements()
            .mapNotNull { it.toFragmentInjectElementsOrNull() }

        fragmentInjectElements
            .associateWith { it.toFragmentInjection() }
            .forEach { writeFragmentInject(it.key, it.value) }

        val fragmentModuleElements = roundEnv.findFragmentModuleTypeElement()
            ?.toFragmentModuleElementsOrNull(fragmentInjectElements)

        if (fragmentModuleElements != null) {
            val moduleType = fragmentModuleElements.moduleType

            val userModuleFqcn = userModule
            if (userModuleFqcn != null) {

                userModule = null
            } else {
                userModule = moduleType.qualifiedName.toString()

                val fragmentInjectionModule = fragmentModuleElements.toFragmentInjectionModule()
                writeFragmentModule(fragmentModuleElements, fragmentInjectionModule)
            }
        }

        // Wait until processing is ending to validate that the @FragmentModule's @Module annotation
        // includes the generated type.
        if (roundEnv.processingOver()) {
            val userModuleFqcn = userModule
            if (userModuleFqcn != null) {
                // In the processing round in which we handle the @WorkerModule the @Module annotation's
                // includes contain an <error> type because we haven't generated the worker module yet.
                // As a result, we need to re-lookup the element so that its referenced types are available.
                val userModule = elements.getTypeElement(userModuleFqcn)

                // Previous validation guarantees this annotation is present.
                val moduleAnnotation = userModule.getAnnotation("dagger.Module")
                // Dagger guarantees this property is present and is an array of types or errors.
                val includes = moduleAnnotation?.getValue("includes", elements)
                    ?.cast<MirrorValue.Array>()
                    ?.filterIsInstance<MirrorValue.Type>() ?: emptyList()

                val generatedModuleName = userModule.toClassName().fragmentInjectModuleName()
                val referencesGeneratedModule = includes
                    .map { it.toTypeName() }
                    .any { it == generatedModuleName }
                if (!referencesGeneratedModule) {
                    error(
                        "@FragmentModule's @Module must include ${generatedModuleName.simpleName()}",
                        userModule
                    )
                }
            }
        }

        return false
    }

    /**
     * Find [TypeElement]s which are candidates for fragment injection by having a constructor
     * annotated with [FragmentInject].
     */
    private fun RoundEnvironment.findFragmentInjectCandidateTypeElements(): List<TypeElement> {
        return findElementsAnnotatedWith<FragmentInject>()
            .map { it.enclosingElement as TypeElement }
    }

    /**
     * From this [TypeElement] which is a candidate for fragment injection, find and validate the
     * syntactical elements required to generate the factory:
     * - Non-private, non-inner target type
     * - Single non-private target constructor
     */
    private fun TypeElement.toFragmentInjectElementsOrNull(): FragmentInjectElements? {
        var valid = true
        if (Modifier.PRIVATE in modifiers) {
            error("@FragmentInject-using types must not be private", this)
            valid = false
        }
        if (enclosingElement.kind == ElementKind.CLASS && Modifier.STATIC !in modifiers) {
            error("Nested @FragmentInject-using types must be static", this)
            valid = false
        }
        if (!types.isSubtype(asType(), fragmentType)) {
            error("@FragmentInject-using types must be a subtype of androidx.fragment.app.Fragment", this)
            valid = false
        }

        val constructors = enclosedElements
            .filter { it.kind == ElementKind.CONSTRUCTOR }
            .filter { it.hasAnnotation<FragmentInject>() }
            .castEach<ExecutableElement>()
        if (constructors.size > 1) {
            error("Multiple @FragmentInject-annotated constructors found.", this)
            valid = false
        }

        if (!valid) return null

        val constructor = constructors.single()
        if (Modifier.PRIVATE in constructor.modifiers) {
            error("@FragmentInject constructor must not be private", this)
            return null
        }

        return FragmentInjectElements(this, constructor)
    }

    private fun FragmentInjectElements.toFragmentInjection(): FragmentInjection {
        val requests = targetConstructor.parameters.map { it.asDependencyRequest() }
        val targetType = targetType.asType().toTypeName()
        val generatedAnnotations = createGeneratedAnnotation(elements)
        return FragmentInjection(targetType, requests, generatedAnnotations)
    }

    private fun writeFragmentInject(elements: FragmentInjectElements, injection: FragmentInjection) {
        val generatedTypeSpec = injection.brewJava()
            .toBuilder()
            .addOriginatingElement(elements.targetType)
            .build()
        JavaFile.builder(injection.generatedType.packageName(), generatedTypeSpec)
            .addFileComment("Generated by @FragmentInject. Do not modify!")
            .build()
            .writeTo(filer)
    }

    private fun RoundEnvironment.findFragmentModuleTypeElement(): TypeElement? {
        val fragmentModules = findElementsAnnotatedWith<FragmentModule>().castEach<TypeElement>()
        if (fragmentModules.size > 1) {
            fragmentModules.forEach {
                error("Multiple @FragmentModule-annotated modules found.", it)
            }
            return null
        }
        return fragmentModules.singleOrNull()
    }

    private fun TypeElement.toFragmentModuleElementsOrNull(
        fragmentInjectElements: List<FragmentInjectElements>
    ): FragmentModuleElements? {
        if (!hasAnnotation("dagger.Module")) {
            error("@FragmentModule must also be annotated as Dagger @Module", this)
            return null
        }

        val fragmentTargetTypes = fragmentInjectElements.map { it.targetType }
        return FragmentModuleElements(this, fragmentTargetTypes)
    }

    private fun FragmentModuleElements.toFragmentInjectionModule(): FragmentInjectionModule {
        val moduleName = moduleType.toClassName()
        val fragmentNames = fragmentTypes.map { it.toClassName() }
        val public = Modifier.PUBLIC in moduleType.modifiers
        val generatedAnnotation = createGeneratedAnnotation(elements)
        return FragmentInjectionModule(moduleName, public, fragmentNames, generatedAnnotation)
    }

    private fun writeFragmentModule(elements: FragmentModuleElements, module: FragmentInjectionModule) {
        val generatedTypeSpec = module.brewJava()
            .toBuilder()
            .addOriginatingElement(elements.moduleType)
            .applyEach(elements.fragmentTypes) {
                addOriginatingElement(it)
            }
            .build()
        JavaFile.builder(module.generatedType.packageName(), generatedTypeSpec)
            .addFileComment("Generated by @FragmentInject. Do not modify!")
            .build()
            .writeTo(filer)
    }

    private fun error(message: String, element: Element? = null) {
        messager.printMessage(ERROR, message, element)
    }

    private data class FragmentInjectElements(
        val targetType: TypeElement,
        val targetConstructor: ExecutableElement
    )

    private data class FragmentModuleElements(
        val moduleType: TypeElement,
        val fragmentTypes: List<TypeElement>
    )
}