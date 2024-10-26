package org.odk.collect.shared.locks

import java.util.function.Function

interface ChangeLock {
    /**
     * @param function some work to be executed after attempting to acquire the lock. The function
     * will be passed `true` if the lock was acquired and `false` if not. The
     * function return value will be returned from the call to this method.
     */
    fun <T> withLock(function: Function<Boolean, T>): T

    fun tryLock(): Boolean

    fun unlock()
}

class ThreadSafeBooleanChangeLock : ChangeLock {
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
        return synchronized(this) {
            if (locked) {
                false
            } else {
                locked = true
                true
            }
        }
    }

    override fun unlock() {
        synchronized(this) {
            locked = false
        }
    }
}
