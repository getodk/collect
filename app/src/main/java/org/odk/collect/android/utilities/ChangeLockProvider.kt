package org.odk.collect.android.utilities

import org.odk.collect.shared.locks.ChangeLock
import org.odk.collect.shared.locks.ReentrantLockChangeLock
import javax.inject.Singleton

@Singleton
class ChangeLockProvider {

    private val locks: MutableMap<String, ChangeLock> = mutableMapOf()

    fun getFormLock(projectId: String): ChangeLock {
        return locks.getOrPut("form:$projectId") { ReentrantLockChangeLock() }
    }

    fun getInstanceLock(projectId: String): ChangeLock {
        return locks.getOrPut("instance:$projectId") { ReentrantLockChangeLock() }
    }
}
