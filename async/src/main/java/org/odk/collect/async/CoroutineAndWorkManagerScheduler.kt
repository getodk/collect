package org.odk.collect.async

import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class CoroutineAndWorkManagerScheduler(
    foregroundContext: CoroutineContext,
    backgroundContext: CoroutineContext,
    private val workManager: WorkManager
) : CoroutineScheduler(foregroundContext, backgroundContext) {

    constructor(workManager: WorkManager) : this(
        Dispatchers.Main,
        Dispatchers.IO,
        workManager
    ) // Needed for Java construction

    private val taskSpecRunner = WorkManagerTaskSpecImmediateRunner(workManager)
    private val taskSpecScheduler = WorkManagerTaskSpecScheduler(workManager)

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
        taskSpecScheduler.schedule(tag, spec, inputData, networkConstraint = networkConstraint)
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
        workManager.cancelUniqueWork(tag)
    }

    override fun isDeferredRunning(tag: String): Boolean {
        return isWorkManagerWorkRunning(tag)
    }

    override fun cancelAllDeferred() {
        workManager.cancelAllWork()
    }

    private fun isWorkManagerWorkRunning(tag: String): Boolean {
        val statuses = workManager.getWorkInfosByTag(tag)
        for (workInfo in statuses.get()) {
            if (workInfo.state == WorkInfo.State.RUNNING) {
                return true
            }
        }

        return false
    }
}
