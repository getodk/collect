package org.odk.collect.androidshared.data

/**
 * Useful for values that are read multiple times but only used
 * once (like an error that shows a dialog once).
 */
data class Consumable<T>(val value: T) {

    private var consumed = false

    fun isConsumed(): Boolean {
        return consumed
    }

    fun consume() {
        consumed = true
    }
}
