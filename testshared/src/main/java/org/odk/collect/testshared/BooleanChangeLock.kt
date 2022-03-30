package org.odk.collect.testshared

import java.util.function.Function
import org.odk.collect.shared.locks.ChangeLock

class BooleanChangeLock : ChangeLock {
    private var locked = false
    override fun <T> withLock(function: Function<Boolean, T>): T {
        return function.apply(!locked)
    }

    fun lock() {
        locked = true
    }
}