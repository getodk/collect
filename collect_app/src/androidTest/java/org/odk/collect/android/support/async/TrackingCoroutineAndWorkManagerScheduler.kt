package org.odk.collect.android.support.async

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.BackoffPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.odk.collect.android.support.async.TestSchedulerTaskSpec.Companion.DATA_WRAPPED_SPEC
import org.odk.collect.async.Cancellable
import org.odk.collect.async.coroutines.CoroutineTaskRunner
import org.odk.collect.async.NotificationInfo
import org.odk.collect.async.Scheduler
import org.odk.collect.async.SchedulerBuilder
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.workmanager.WorkManagerTaskSpecScheduler
import org.odk.collect.async.network.NetworkStateProvider
import org.odk.collect.async.services.ForegroundServiceTaskSpecRunner
import java.util.function.Consumer
import java.util.function.Supplier

class TrackingCoroutineAndWorkManagerScheduler(private val networkStateProvider: NetworkStateProvider) : Scheduler {

    private val wrappedScheduler: Scheduler
    private val deferredTasks: MutableList<DeferredTask> = ArrayList()
    private val backgroundDispatcher = TrackingCoroutineDispatcher(Dispatchers.IO)

    init {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val workManager = WorkManager.getInstance(application)
        wrappedScheduler = SchedulerBuilder.build(
            CoroutineTaskRunner(Dispatchers.Main, backgroundDispatcher),
            ForegroundServiceTaskSpecRunner(application),
            WorkManagerTaskSpecScheduler(workManager)
        )
    }

    override fun repeat(foreground: Runnable, repeatPeriod: Long): Cancellable {
        return wrappedScheduler.repeat({ foreground.run() }, repeatPeriod)
    }

    override fun <T> immediate(background: Supplier<T>, foreground: Consumer<T>) {
        AsyncWorkTracker.startWork()
        wrappedScheduler.immediate(background) { t: T ->
            foreground.accept(t)
            AsyncWorkTracker.finishWork()
        }
    }

    override fun immediate(foreground: Boolean, delay: Long?, runnable: Runnable) {
        AsyncWorkTracker.startWork()
        wrappedScheduler.immediate(foreground, delay) {
            runnable.run()
            AsyncWorkTracker.finishWork()
        }
    }

    override fun immediate(
        tag: String,
        spec: TaskSpec,
        inputData: Map<String, String>,
        notificationInfo: NotificationInfo
    ) {
        AsyncWorkTracker.startWork()
        val augmentedInputData = inputData + Pair(DATA_WRAPPED_SPEC, spec.javaClass.name)
        wrappedScheduler.immediate(
            tag,
            TestSchedulerTaskSpec(),
            augmentedInputData,
            notificationInfo
        )
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

    @JvmOverloads
    fun runDeferredTasks(isLastUniqueExecution: Boolean = true, isStopped: Boolean = false) {
        if (networkStateProvider.isDeviceOnline) {
            val applicationContext = ApplicationProvider.getApplicationContext<Context>()
            deferredTasks.removeIf { deferredTask ->
                if (deferredTask.networkConstraint == null || deferredTask.networkConstraint == networkStateProvider.currentNetwork) {
                    deferredTask.spec.getTask(applicationContext, deferredTask.inputData, isLastUniqueExecution) { isStopped }
                        .get()
                    deferredTask.repeatPeriod == null
                } else {
                    false
                }
            }
        }
    }

    fun getDeferredTasks(): List<DeferredTask> {
        return deferredTasks
    }

    override fun cancelAllDeferred() {}

    override fun <T> flowOnBackground(flow: Flow<T>): Flow<T> {
        return flow.flowOn(backgroundDispatcher)
    }

    class DeferredTask(
        val tag: String,
        val spec: TaskSpec,
        val repeatPeriod: Long?,
        val inputData: Map<String, String>,
        val networkConstraint: Scheduler.NetworkType?
    )
}

class TestSchedulerTaskSpec : TaskSpec {
    override val maxRetries: Int? = null
    override val backoffPolicy: BackoffPolicy? = null
    override val backoffDelay: Long? = null

    private lateinit var wrappedSpec: TaskSpec

    override fun getTask(
        context: Context,
        inputData: Map<String, String>,
        isLastUniqueExecution: Boolean,
        isStopped: () -> Boolean
    ): Supplier<Boolean> {
        wrappedSpec = Class.forName(inputData[DATA_WRAPPED_SPEC]!!)
            .getConstructor()
            .newInstance() as TaskSpec

        return Supplier {
            val result =
                wrappedSpec.getTask(context, inputData, isLastUniqueExecution, isStopped).get()

            AsyncWorkTracker.finishWork()
            result
        }
    }

    override fun onException(exception: Throwable) {
        wrappedSpec.onException(exception)
    }

    companion object {
        const val DATA_WRAPPED_SPEC = "wrapped_spec"
    }
}
