package org.odk.collect.shared.locks

import java.util.function.Function

class ThreadSafeBooleanChangeLock : ChangeLock {
    private var currentOwnerId: String? = null

    override fun <T> withLock(function: Function<Boolean, T>): T {
        val acquired = tryLock(DEFAULT_LOCK_OWNER_ID)

        return try {
            function.apply(acquired)
        } finally {
            if (acquired) {
                unlock(DEFAULT_LOCK_OWNER_ID)
            }
        }
    }

    override fun tryLock(ownerId: String): Boolean {
        return synchronized(this) {
            if (currentOwnerId != null) {
                false
            } else {
                currentOwnerId = ownerId
                true
            }
        }
    }

    override fun lock(ownerId: String) {
        synchronized(this) {
            if (currentOwnerId != null) {
                throw IllegalStateException()
            } else {
                currentOwnerId = ownerId
            }
        }
    }

    override fun unlock(ownerId: String) {
        synchronized(this) {
            if (currentOwnerId == ownerId) {
                currentOwnerId = null
            }
        }
    }
}
