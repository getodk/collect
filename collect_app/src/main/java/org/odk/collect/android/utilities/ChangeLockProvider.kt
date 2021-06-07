package org.odk.collect.android.utilities

import org.odk.collect.shared.locks.ChangeLock
import org.odk.collect.shared.locks.ReentrantLockChangeLock
import javax.inject.Singleton

@Singleton
class ChangeLockProvider {

    private val formLock: ChangeLock = ReentrantLockChangeLock()

    fun getFormLock(): ChangeLock {
        return formLock
    }
}
