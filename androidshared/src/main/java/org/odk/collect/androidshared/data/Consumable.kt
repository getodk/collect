package org.odk.collect.androidshared.data

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

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

fun <T> LiveData<Consumable<T>>.consume(lifecycleOwner: LifecycleOwner, consumer: (T) -> Unit) {
    observe(lifecycleOwner) { consumable ->
        if (!consumable.isConsumed()) {
            consumable.consume()
            consumer(consumable.value)
        }
    }
}
