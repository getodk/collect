package org.odk.collect.shared.locks

import java.util.function.Function

class BooleanChangeLock : ChangeLock {
    private var locked = false

    override fun <T> withLock(function: Function<Boolean, T>): T {
        val acquired = tryLock()

        return try {
            function.apply(acquired)
        } finally {
            if (acquired) {
                unlock()
            }
        }
    }

    override fun tryLock(): Boolean {
        if (locked) {
            return false
        } else {
            locked = true
            return true
        }
    }

    override fun lock() {
        if (locked) {
            throw IllegalStateException()
        } else {
            locked = true
        }
    }

    override fun unlock() {
        locked = false
    }
}
