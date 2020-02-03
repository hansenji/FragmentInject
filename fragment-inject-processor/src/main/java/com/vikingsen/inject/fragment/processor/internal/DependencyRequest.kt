package com.vikingsen.inject.fragment.processor.internal

import javax.lang.model.element.VariableElement

/** Associates a [Key] with its desired use as assisted or not. */
data class DependencyRequest(
    val namedKey: NamedKey
) {
    val key get() = namedKey.key
    val name get() = namedKey.name

    override fun toString() = "$key $name"
}

fun VariableElement.asDependencyRequest() =
    DependencyRequest(asNamedKey())
