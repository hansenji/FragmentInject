package com.vikingsen.inject.fragment.processor

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import com.vikingsen.inject.fragment.processor.internal.DependencyRequest
import com.vikingsen.inject.fragment.processor.internal.Key
import com.vikingsen.inject.fragment.processor.internal.applyEach
import com.vikingsen.inject.fragment.processor.internal.joinToCode
import com.vikingsen.inject.fragment.processor.internal.peerClassWithReflectionNesting
import com.vikingsen.inject.fragment.processor.internal.rawClassName
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC

private val JAVAX_INJECT = ClassName.get("javax.inject", "Inject")
private val JAVAX_PROVIDER = ClassName.get("javax.inject", "Provider")
private val FACTORY = ClassName.get("com.vikingsen.inject.fragment", "FragmentInjectFactory")
private val FRAGMENT = ClassName.get("androidx.fragment.app", "Fragment")

class FragmentInjection(
    private val targetType: TypeName,
    private val requests: List<DependencyRequest>,
    private val generatedAnnotations: AnnotationSpec?
) {
    /** The type generated from [brewJava]. */
    val generatedType = targetType.rawClassName().injectFactoryName()

    fun brewJava(): TypeSpec {
        return TypeSpec.classBuilder(generatedType)
            .addModifiers(PUBLIC, FINAL)
            .addSuperinterface(FACTORY)
            .apply { generatedAnnotations?.let { addAnnotation(it) } }
            .applyEach(requests) {
                addField(it.providerType.withoutAnnotations(), it.name, PRIVATE, FINAL)
            }
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
                    .addAnnotation(JAVAX_INJECT)
                    .applyEach(requests) {
                        addParameter(it.providerType, it.name)
                        addStatement("this.$1N = $1N", it.name)
                    }
                    .build()
            )
            .addMethod(
                MethodSpec.methodBuilder("create")
                    .addAnnotation(Override::class.java)
                    .addModifiers(PUBLIC)
                    .returns(FRAGMENT)
                    .apply {
                        if (targetType is ParameterizedTypeName) {
                            addTypeVariables(targetType.typeArguments.filterIsInstance<TypeVariableName>())
                        }
                    }
                    .addStatement(
                        "return new \$T(\n\$L)", targetType,
                        requests.map { it.argumentProvider }.joinToCode(",\n")
                    )
                    .build()
            )
            .build()
    }
}

/** True when this key represents a parameterized JSR 330 `Provider`. */
private val Key.isProvider get() = type is ParameterizedTypeName && type.rawType == JAVAX_PROVIDER

private val DependencyRequest.providerType: TypeName
    get() {
        val type = if (key.isProvider) {
            key.type // Do not wrap a Provider inside another Provider.
        } else {
            ParameterizedTypeName.get(JAVAX_PROVIDER, key.type)
        }
        key.qualifier?.let {
            return type.annotated(it)
        }
        return type
    }

private val DependencyRequest.argumentProvider
    get() = CodeBlock.of(if (key.isProvider) "\$N" else "\$N.get()", name)

fun ClassName.injectFactoryName(): ClassName = peerClassWithReflectionNesting("${simpleName()}_InjectFactory")