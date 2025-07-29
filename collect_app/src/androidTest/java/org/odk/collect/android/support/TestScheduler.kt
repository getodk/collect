package org.odk.collect.android.support

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.Flow
import org.odk.collect.async.Cancellable
import org.odk.collect.async.CoroutineAndWorkManagerScheduler
import org.odk.collect.async.NotificationInfo
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.network.NetworkStateProvider
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext

class TestScheduler(private val networkStateProvider: NetworkStateProvider) : Scheduler, CoroutineDispatcher() {

    private val wrappedScheduler: Scheduler
    private val lock = Any()
    private var tasks = 0
    private var finishedCallback: Runnable? = null
    private val deferredTasks: MutableList<DeferredTask> = ArrayList()
    private val backgroundDispatcher = Dispatchers.IO

    init {
        val workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
        wrappedScheduler = CoroutineAndWorkManagerScheduler(Dispatchers.Main, this, workManager)
    }

    override fun repeat(foreground: Runnable, repeatPeriod: Long): Cancellable {
        return wrappedScheduler.repeat({ foreground.run() }, repeatPeriod)
    }

    override fun <T> immediate(background: Supplier<T>, foreground: Consumer<T>) {
        increment()
        wrappedScheduler.immediate(background) { t: T ->
            foreground.accept(t)
            decrement()
        }
    }

    override fun immediate(foreground: Boolean, delay: Long?, runnable: Runnable) {
        increment()
        wrappedScheduler.immediate(foreground, delay) {
            runnable.run()
            decrement()
        }
    }

    override fun immediate(
        tag: String,
        spec: TaskSpec,
        inputData: Map<String, String>,
        notificationInfo: NotificationInfo
    ) {
        increment()
        val context = ApplicationProvider.getApplicationContext<Context>()
        wrappedScheduler.immediate {
            spec.getTask(context, inputData, true).get()
            decrement()
        }
    }

    override fun networkDeferred(
        tag: String,
        spec: TaskSpec,
        inputData: Map<String, String>,
        networkConstraint: Scheduler.NetworkType?
    ) {
        cancelDeferred(tag)
        deferredTasks.add(DeferredTask(tag, spec, null, inputData, networkConstraint))
    }

    override fun networkDeferredRepeat(
        tag: String,
        spec: TaskSpec,
        repeatPeriod: Long,
        inputData: Map<String, String>
    ) {
        cancelDeferred(tag)
        deferredTasks.add(DeferredTask(tag, spec, repeatPeriod, inputData, null))
    }

    override fun cancelDeferred(tag: String) {
        deferredTasks.removeIf { t: DeferredTask -> t.tag == tag }
    }

    override fun isDeferredRunning(tag: String): Boolean {
        return wrappedScheduler.isDeferredRunning(tag)
    }

    fun runDeferredTasks() {
        if (networkStateProvider.isDeviceOnline) {
            val applicationContext = ApplicationProvider.getApplicationContext<Context>()
            deferredTasks.removeIf { deferredTask ->
                if (deferredTask.networkConstraint == null || deferredTask.networkConstraint == networkStateProvider.currentNetwork) {
                    deferredTask.spec.getTask(applicationContext, deferredTask.inputData, true)
                        .get()
                    deferredTask.repeatPeriod == null
                } else {
                    false
                }
            }
        }
    }

    fun setFinishedCallback(callback: Runnable?) {
        finishedCallback = callback
    }

    private fun increment() {
        synchronized(lock) { tasks++ }
    }

    private fun decrement() {
        synchronized(lock) {
            tasks--
            if (tasks == 0 && finishedCallback != null) {
                finishedCallback!!.run()
            }
        }
    }

    val taskCount: Int
        get() {
            synchronized(lock) { return tasks }
        }

    fun getDeferredTasks(): List<DeferredTask> {
        return deferredTasks
    }

    override fun cancelAllDeferred() {}

    override fun <T> flowOnBackground(flow: Flow<T>): Flow<T> {
        return wrappedScheduler.flowOnBackground(flow)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        increment()
        backgroundDispatcher.dispatch(context) {
            block.run()
            decrement()
        }
    }

    class DeferredTask(
        val tag: String,
        val spec: TaskSpec,
        val repeatPeriod: Long?,
        val inputData: Map<String, String>,
        val networkConstraint: Scheduler.NetworkType?
    )
}
