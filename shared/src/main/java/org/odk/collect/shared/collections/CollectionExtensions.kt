package org.odk.collect.shared.collections

import kotlin.math.abs

object CollectionExtensions {

    fun <T> List<T>.itemFromHashOf(any: Any): T {
        val index = abs(any.hashCode()) % this.size
        return this[index]
    }
}
