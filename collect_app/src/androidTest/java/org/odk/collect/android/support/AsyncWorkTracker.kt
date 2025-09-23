package org.odk.collect.android.support

object AsyncWorkTracker {
    private var runningTasks = 0

    @JvmStatic
    val taskCount: Int
        get() {
            synchronized(this) { return runningTasks }
        }

    fun startWork() {
        synchronized(this) { runningTasks++ }
    }

    fun finishWork() {
        synchronized(this) {
            if (runningTasks > 0) {
                runningTasks--
            } else {
                throw IllegalStateException()
            }
        }
    }
}
