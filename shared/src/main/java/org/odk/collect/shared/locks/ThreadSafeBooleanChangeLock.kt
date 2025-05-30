package org.odk.collect.shared.locks

class ThreadSafeBooleanChangeLock : ChangeLock {
    private var currentOwnerId: String? = null

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
