package org.odk.collect.android.support.async

import java.util.concurrent.atomic.AtomicInteger

object AsyncWorkTracker {
    private val runningTasks = AtomicInteger(0)

    @JvmStatic
    val taskCount: Int
        get() {
            return runningTasks.get()
        }

    fun startWork() {
        runningTasks.incrementAndGet()
    }

    fun finishWork() {
        val decremented = runningTasks.decrementAndGet()
        if (decremented < 0) {
            throw IllegalStateException()
        }
    }
}
