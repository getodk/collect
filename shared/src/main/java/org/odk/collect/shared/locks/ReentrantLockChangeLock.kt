package org.odk.collect.shared.locks

import java.util.concurrent.locks.ReentrantLock
import java.util.function.Function

class ReentrantLockChangeLock : ChangeLock {
    private val lock = ReentrantLock()

    override fun <T> withLock(function: Function<Boolean, T>): T {
        return try {
            function.apply(lock.tryLock())
        } finally {
            unlock()
        }
    }

    override fun tryLock(): Boolean {
        return lock.tryLock()
    }

    override fun unlock() {
        try {
            lock.unlock()
        } catch (ignored: IllegalMonitorStateException) {
            // Ignored
        }
    }
}
