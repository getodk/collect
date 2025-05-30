package org.odk.collect.shared.locks

import java.util.function.Function

const val DEFAULT_LOCK_OWNER_ID = "ownerId"

interface ChangeLock {
    /**
     * @param function some work to be executed after attempting to acquire the lock. The function
     * will be passed `true` if the lock was acquired and `false` if not. The
     * function return value will be returned from the call to this method.
     */
    fun <T> withLock(function: Function<Boolean, T>): T {
        val acquired = tryLock(DEFAULT_LOCK_OWNER_ID)

        return try {
            function.apply(acquired)
        } finally {
            if (acquired) {
                unlock(DEFAULT_LOCK_OWNER_ID)
            }
        }
    }

    fun tryLock(ownerId: String): Boolean

    fun lock(ownerId: String)

    fun unlock(ownerId: String)
}
