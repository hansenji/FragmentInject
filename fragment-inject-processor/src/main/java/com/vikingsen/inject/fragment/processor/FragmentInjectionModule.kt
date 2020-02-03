package com.vikingsen.inject.fragment.processor

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.vikingsen.inject.fragment.processor.internal.applyEach
import com.vikingsen.inject.fragment.processor.internal.peerClassWithReflectionNesting
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC

private val MODULE = ClassName.get("dagger", "Module")
private val BINDS = ClassName.get("dagger", "Binds")
private val INTO_MAP = ClassName.get("dagger.multibindings", "IntoMap")
private val CLASS_KEY = ClassName.get("dagger.multibindings", "ClassKey")
private val FRAGMENT_INJECT_FACTORY = ClassName.get("com.vikingsen.inject.fragment", "FragmentInjectFactory")


class FragmentInjectionModule(
    moduleName: ClassName,
    private val public: Boolean,
    private val injectedNames: List<ClassName>,
    /** An optional `@Generated` annotation marker. */
    private val generatedAnnotation: AnnotationSpec? = null
) {
    val generatedType = moduleName.fragmentInjectModuleName()

    fun brewJava(): TypeSpec {
        return TypeSpec.classBuilder(generatedType)
            .addAnnotation(MODULE)
            .apply { generatedAnnotation?.let { addAnnotation(it) } }
            .addModifiers(ABSTRACT)
            .apply { if (public) addModifiers(PUBLIC) }
            .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build())
            .applyEach(injectedNames) { injectedName ->
                addMethod(
                    MethodSpec.methodBuilder(injectedName.bindMethodName())
                        .addAnnotation(BINDS)
                        .addAnnotation(INTO_MAP)
                        .addAnnotation(AnnotationSpec.builder(CLASS_KEY).addMember("value", "\$T.class", injectedName).build())
                        .addModifiers(ABSTRACT)
                        .returns(FRAGMENT_INJECT_FACTORY)
                        .addParameter(injectedName.injectFactoryName(), "factory")
                        .build()
                )
            }
            .build()
    }
}

private fun ClassName.bindMethodName() = "bind_" + reflectionName().replace('.', '_')
internal fun ClassName.fragmentInjectModuleName() = peerClassWithReflectionNesting("""FragmentInject_${simpleName()}""")