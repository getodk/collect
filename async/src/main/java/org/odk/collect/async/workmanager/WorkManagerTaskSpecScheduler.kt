package org.odk.collect.async.workmanager

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.TaskSpecScheduler
import org.odk.collect.async.workmanager.TaskSpecWorker.Companion.setup
import java.util.concurrent.TimeUnit

class WorkManagerTaskSpecScheduler(private val workManager: WorkManager) : TaskSpecScheduler {
    override fun schedule(
        tag: String,
        specs: List<TaskSpec>,
        inputData: Map<String, String>,
        networkConstraint: Scheduler.NetworkType?,
        repeatPeriod: Long?
    ) {
        if (repeatPeriod != null) {
            val workRequest = PeriodicWorkRequest.Builder(
                PeriodicTaskSpecWorkerEnqueuerWorker::class.java,
                repeatPeriod,
                TimeUnit.MILLISECONDS
            ).setup(tag, specs, inputData, networkConstraint).build()

            workManager.enqueueUniquePeriodicWork(
                tag,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        } else {
            val workRequest = TaskSpecWorker.getWorkRequest(
                specs,
                tag,
                inputData,
                networkConstraint
            )

            workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    override fun isRunning(tag: String): Boolean {
        val statuses = workManager.getWorkInfosByTag(tag)
        for (workInfo in statuses.get()) {
            if (workInfo.state == WorkInfo.State.RUNNING) {
                return true
            }
        }

        return false
    }

    override fun cancel(tag: String) {
        workManager.cancelUniqueWork(tag)
        workManager.cancelUniqueWork(PeriodicTaskSpecWorkerEnqueuerWorker.chainTaskName(tag))
    }

    override fun cancelAll() {
        workManager.cancelAllWork()
    }
}
