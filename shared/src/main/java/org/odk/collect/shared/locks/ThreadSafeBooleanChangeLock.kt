package org.odk.collect.shared.locks

class ThreadSafeBooleanChangeLock : ChangeLock {
    private var currentToken: Any? = null

    override fun tryLock(token: Any): Boolean {
        return synchronized(this) {
            if (currentToken != null) {
                false
            } else {
                currentToken = token
                true
            }
        }
    }

    override fun unlock(token: Any) {
        synchronized(this) {
            if (currentToken == token) {
                currentToken = null
            }
        }
    }
}
