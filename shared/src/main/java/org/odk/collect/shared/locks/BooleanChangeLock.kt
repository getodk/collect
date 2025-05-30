package org.odk.collect.shared.locks

class BooleanChangeLock : ChangeLock {
    private var currentToken: Any? = null

    override fun tryLock(token: Any): Boolean {
        if (currentToken != null) {
            return false
        } else {
            currentToken = token
            return true
        }
    }

    override fun unlock(token: Any) {
        if (currentToken == token) {
            currentToken = null
        }
    }

    fun lock(token: Any) {
        if (currentToken != null) {
            throw IllegalStateException()
        } else {
            currentToken = token
        }
    }
}
