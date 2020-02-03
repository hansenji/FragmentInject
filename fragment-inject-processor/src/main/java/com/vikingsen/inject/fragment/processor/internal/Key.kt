package com.vikingsen.inject.fragment.processor.internal

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

private val keyComparator = compareBy<Key>({ it.type.toString() }, { it.qualifier == null })

/** Represents a type and an optional qualifier annotation for a binding. */
data class Key(
    val type: TypeName,
    val qualifier: AnnotationSpec? = null
) : Comparable<Key> {
    override fun toString() = qualifier?.let { "$it $type" } ?: type.toString()
    override fun compareTo(other: Key) = keyComparator.compare(this, other)
}

/** Create a [Key] from this type and any qualifier annotation. */
fun VariableElement.asKey(mirror: TypeMirror = asType()) = Key(mirror.toTypeName(),
    annotationMirrors.find {
        it.annotationType.asElement().hasAnnotation("javax.inject.Qualifier")
    }?.let { AnnotationSpec.get(it) })
