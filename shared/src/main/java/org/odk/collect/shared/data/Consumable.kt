package org.odk.collect.shared.data

data class Consumable<T>(val value: T) {

    private var consumed = false

    fun isConsumed(): Boolean {
        return consumed
    }

    fun consume() {
        consumed = true
    }
}
