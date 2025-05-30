package org.odk.collect.shared.locks

import java.util.function.Function

class BooleanChangeLock : ChangeLock {
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
        if (currentOwnerId != null) {
            return false
        } else {
            currentOwnerId = ownerId
            return true
        }
    }

    override fun lock(ownerId: String) {
        if (currentOwnerId != null) {
            throw IllegalStateException()
        } else {
            currentOwnerId = ownerId
        }
    }

    override fun unlock(ownerId: String) {
        if (currentOwnerId == ownerId) {
            currentOwnerId = null
        }
    }
}
