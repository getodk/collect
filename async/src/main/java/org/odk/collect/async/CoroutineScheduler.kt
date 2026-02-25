package org.odk.collect.async

import kotlinx.coroutines.flow.Flow
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext

open class CoroutineScheduler(
    foregroundContext: CoroutineContext,
    backgroundContext: CoroutineContext
) : Scheduler {

    private val taskRunner = CoroutineTaskRunner(foregroundContext, backgroundContext)

    override fun <T> immediate(background: Supplier<T>, foreground: Consumer<T>) {
        taskRunner.immediate(background, foreground)
    }

    override fun immediate(foreground: Boolean, delay: Long?, runnable: Runnable) {
        taskRunner.immediate(
            runnable,
            isForeground = foreground,
            delay = delay,
            repeatPeriod = null
        )
    }

    override fun repeat(foreground: Runnable, repeatPeriod: Long): Cancellable {
        return taskRunner.immediate(
            foreground,
            isForeground = true,
            delay = null,
            repeatPeriod = repeatPeriod
        )
    }

    override fun <T> flowOnBackground(flow: Flow<T>): Flow<T> {
        return taskRunner.flowOnBackground(flow)
    }

    override fun cancelAllDeferred() {
        throw UnsupportedOperationException()
    }

    override fun networkDeferred(
        tag: String,
        spec: TaskSpec,
        inputData: Map<String, String>,
        networkConstraint: Scheduler.NetworkType?
    ) {
        throw UnsupportedOperationException()
    }

    override fun networkDeferredRepeat(
        tag: String,
        spec: TaskSpec,
        repeatPeriod: Long,
        inputData: Map<String, String>
    ) {
        throw UnsupportedOperationException()
    }

    override fun cancelDeferred(tag: String) {
        throw UnsupportedOperationException()
    }

    override fun isDeferredRunning(tag: String): Boolean {
        throw UnsupportedOperationException()
    }

    override fun immediate(
        tag: String,
        spec: TaskSpec,
        inputData: Map<String, String>,
        notificationInfo: NotificationInfo
    ) {
        throw UnsupportedOperationException()
    }
}
