package org.odk.collect.testshared

import org.odk.collect.shared.locks.ChangeLock
import java.util.function.Function

class BooleanChangeLock : ChangeLock {
    private var isLocked = false

    override fun <T> withLock(function: Function<Boolean, T>): T {
        return function.apply(!isLocked)
    }

    override fun isLocked(): Boolean {
        return isLocked
    }

    override fun lock() {
        isLocked = true
    }

    override fun unlock() {
        isLocked = false
    }
}
