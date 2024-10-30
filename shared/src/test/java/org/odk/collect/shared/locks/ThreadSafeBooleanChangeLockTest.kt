package org.odk.collect.shared.locks

class ThreadSafeBooleanChangeLockTest : ChangeLockTest() {
    override fun buildSubject(): ChangeLock {
        return ThreadSafeBooleanChangeLock()
    }
}
