package org.odk.collect.shared.locks

class BooleanChangeLockTest : ChangeLockTest() {
    override fun buildSubject(): ChangeLock {
        return BooleanChangeLock()
    }
}
