package org.odk.collect.testshared

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import java.util.LinkedList
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext

class FakeScheduler : Scheduler {

    private val backgroundDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            backgroundTasks.add(block)
        }
    }

    private var foregroundTasks = LinkedList<Runnable>()
    private var backgroundTasks = LinkedList<Runnable>()
    private var repeatTasks = ArrayList<RepeatTask>()

    override fun <T> immediate(background: Supplier<T>, foreground: Consumer<T>) {
        backgroundTasks.addLast(
            Runnable {
                val result = background.get()
                foregroundTasks.add(Runnable { foreground.accept(result) })
            }
        )
    }

    override fun immediate(background: Boolean, runnable: Runnable) {
        if (background) {
            backgroundTasks.push(runnable)
        } else {
            foregroundTasks.push(runnable)
        }
    }

    override fun networkDeferred(tag: String, spec: TaskSpec, inputData: Map<String, String>) {}

    override fun networkDeferred(
        tag: String,
        taskSpec: TaskSpec,
        repeatPeriod: Long,
        inputData: Map<String, String>
    ) {
    }

    override fun repeat(foreground: Runnable, repeatPeriod: Long): Cancellable {
        val task = RepeatTask(repeatPeriod, foreground, null)
        repeatTasks.add(task)

        return object : Cancellable {
            override fun cancel(): Boolean {
                repeatTasks.remove(task)
                return true
            }
        }
    }

    override fun cancelAllDeferred() {}

    override fun <T> flowOnBackground(flow: Flow<T>): Flow<T> {
        return flow.flowOn(backgroundDispatcher)
    }

    fun runForeground() {
        while (foregroundTasks.isNotEmpty()) {
            foregroundTasks.remove().run()
        }

        repeatTasks.forEach { it.runnable.run() }
    }

    fun runForeground(currentTime: Long) {
        while (foregroundTasks.isNotEmpty()) {
            foregroundTasks.remove().run()
        }

        repeatTasks.forEach {
            it.lastRun.let { lastRun ->
                if (lastRun == null) {
                    it.runnable.run()
                    it.lastRun = currentTime
                } else if ((currentTime - lastRun) >= it.interval) {
                    it.runnable.run()
                    it.lastRun = currentTime
                }
            }
        }
    }

    fun runBackground() {
        while (backgroundTasks.isNotEmpty()) {
            backgroundTasks.remove().run()
        }
    }

    fun flush() {
        while (backgroundTasks.isNotEmpty() || foregroundTasks.isNotEmpty()) {
            runBackground()
            runForeground()
        }
    }

    fun isRepeatRunning(): Boolean {
        return repeatTasks.isNotEmpty()
    }

    override fun isDeferredRunning(tag: String): Boolean {
        return false
    }

    override fun cancelDeferred(tag: String) {}
}

fun <T> LiveData<T>.getOrAwaitValue(
    scheduler: FakeScheduler
): T {
    return this.getOrAwaitValue { scheduler.flush() }
}

private data class RepeatTask(val interval: Long, val runnable: Runnable, var lastRun: Long?)
