package org.odk.collect.async

import kotlinx.coroutines.flow.Flow
import java.util.function.Consumer
import java.util.function.Supplier

object SchedulerBuilder {

    @JvmStatic
    fun build(
        taskRunner: TaskRunner,
        taskSpecRunner: TaskSpecRunner,
        taskSpecScheduler: TaskSpecScheduler
    ): Scheduler {
        return object : Scheduler {
            override fun <T> immediate(
                background: Supplier<T>,
                foreground: Consumer<T>
            ) {
                taskRunner.immediate(background, foreground)
            }

            override fun immediate(
                foreground: Boolean,
                delay: Long?,
                runnable: Runnable
            ) {
                taskRunner.immediate(
                    runnable,
                    isForeground = foreground,
                    delay = delay,
                    repeatPeriod = null
                )
            }

            override fun immediate(
                tag: String,
                spec: TaskSpec,
                inputData: Map<String, String>,
                notificationInfo: NotificationInfo
            ) {
                taskSpecRunner.run(tag, spec, inputData, notificationInfo)
            }

            override fun networkDeferred(
                tag: String,
                spec: TaskSpec,
                inputData: Map<String, String>,
                networkConstraint: Scheduler.NetworkType?
            ) {
                taskSpecScheduler.schedule(
                    tag,
                    spec,
                    inputData,
                    networkConstraint = networkConstraint
                )
            }

            override fun networkDeferredRepeat(
                tag: String,
                spec: TaskSpec,
                repeatPeriod: Long,
                inputData: Map<String, String>
            ) {
                taskSpecScheduler.schedule(tag, spec, inputData, repeatPeriod = repeatPeriod)
            }

            override fun cancelDeferred(tag: String) {
                taskSpecScheduler.cancel(tag)
            }

            override fun isDeferredRunning(tag: String): Boolean {
                return taskSpecScheduler.isRunning(tag)
            }

            override fun repeat(
                foreground: Runnable,
                repeatPeriod: Long
            ): Cancellable {
                return taskRunner.immediate(
                    foreground,
                    isForeground = true,
                    delay = null,
                    repeatPeriod = repeatPeriod
                )
            }

            override fun cancelAllDeferred() {
                taskSpecScheduler.cancelAll()
            }

            override fun <T> flowOnBackground(flow: Flow<T>): Flow<T> {
                return taskRunner.flowOnBackground(flow)
            }

        }
    }
}
