package org.odk.collect.android.utilities

import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.shared.locks.ChangeLock
import org.odk.collect.shared.locks.ThreadSafeBooleanChangeLock
import javax.inject.Singleton

@Singleton
class ChangeLockProvider(private val changeLockFactory: () -> ChangeLock = { ThreadSafeBooleanChangeLock() }) :
    ProjectDependencyFactory<ChangeLocks> {

    private val locks: MutableMap<String, ChangeLock> = mutableMapOf()

    @Deprecated(message = "Use create() instead")
    fun getFormLock(projectId: String): ChangeLock {
        return locks.getOrPut("form:$projectId") { changeLockFactory() }
    }

    @Deprecated(message = "Use create() instead")
    fun getInstanceLock(projectId: String): ChangeLock {
        return locks.getOrPut("instance:$projectId") { changeLockFactory() }
    }

    override fun create(projectId: String): ChangeLocks {
        return ChangeLocks(getFormLock(projectId), getInstanceLock(projectId))
    }
}

data class ChangeLocks(val formsLock: ChangeLock, val instancesLock: ChangeLock)
