package org.odk.collect.shared.locks

import java.util.function.Function

interface ChangeLock {
    /**
     * @param function some work to be executed after attempting to acquire the lock. The function
     * will be passed `true` if the lock was acquired and `false` if not. The
     * function return value will be returned from the call to this method.
     */
    fun <T> withLock(function: Function<Boolean, T>): T {
        val acquired = tryLock(defaultToken)

        return try {
            function.apply(acquired)
        } finally {
            if (acquired) {
                unlock(defaultToken)
            }
        }
    }

    fun tryLock(token: Any): Boolean

    fun lock(token: Any)

    fun unlock(token: Any)

    companion object {
        val defaultToken = Any()
    }
}
