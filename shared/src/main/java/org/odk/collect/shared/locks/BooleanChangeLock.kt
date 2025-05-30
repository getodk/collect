package org.odk.collect.shared.locks

class BooleanChangeLock : ChangeLock {
    private var currentOwnerId: String? = null

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
