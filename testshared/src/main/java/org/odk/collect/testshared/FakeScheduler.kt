package org.odk.collect.testshared

import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import java.util.function.Consumer
import java.util.function.Supplier

class FakeScheduler : Scheduler {

    private var foregroundTask: Runnable? = null
    private var backgroundTask: Runnable? = null
    private var lastRepeatRun: Long = 0
    private var repeatTask: Pair<Long, Runnable>? = null
    private var cancelled = false
    private var isRepeatRunning = false

    override fun <T> immediate(foreground: Supplier<T>, background: Consumer<T>) {
        backgroundTask = Runnable { background.accept(foreground.get()) }
    }

    override fun immediate(foreground: Runnable) {
        foregroundTask = foreground
    }

    override fun networkDeferred(tag: String, spec: TaskSpec) {}

    override fun networkDeferred(tag: String, taskSpec: TaskSpec, repeatPeriod: Long) {}

    override fun repeat(foreground: Runnable, repeatPeriod: Long): Cancellable {
        foregroundTask = foreground
        repeatTask = Pair(repeatPeriod, foreground)
        isRepeatRunning = true
        return object : Cancellable {
            override fun cancel(): Boolean {
                isRepeatRunning = false
                cancelled = true
                return true
            }
        }
    }

    fun runForeground() {
        if (foregroundTask != null) {
            foregroundTask!!.run()
            foregroundTask = null
        }

        repeatTask?.let {
            it.second.run()
        }
    }

    fun runForeground(currentTime: Long) {
        if (foregroundTask != null) {
            foregroundTask!!.run()
            foregroundTask = null
        }

        repeatTask?.let {
            if ((currentTime - lastRepeatRun) >= it.first) {
                it.second.run()
                lastRepeatRun = currentTime
            }
        }
    }

    fun runBackground() {
        if (backgroundTask != null) {
            backgroundTask!!.run()
            backgroundTask = null
        }
    }

    fun hasBeenCancelled(): Boolean {
        return cancelled
    }

    fun isRepeatRunning(): Boolean {
        return isRepeatRunning
    }

    override fun isRunning(tag: String): Boolean {
        return false
    }

    override fun cancelDeferred(tag: String) {}
}
