package org.odk.collect.shared.collections

import org.json.JSONArray
import kotlin.math.abs

object CollectionExtensions {

    fun <T> List<T>.itemFromHashOf(any: Any): T {
        val index = abs(any.hashCode()) % this.size
        return this[index]
    }

    fun JSONArray.has(value: Any): Boolean {
        for (i in 0 until this.length()) {
            if (this[i] == value) {
                return true
            }
        }
        return false
    }
}
