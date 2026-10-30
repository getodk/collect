package org.odk.collect.async.workmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.odk.collect.async.TaskSpec

class PeriodicTaskSpecWorkerEnqueuerWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val workManager = WorkManager.getInstance(context)
        val tag = tags.first()

        val enqueuedOrRunningTasks = workManager.getWorkInfosForUniqueWork(tag).get()
            .filter { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
        if (enqueuedOrRunningTasks.isNotEmpty()) {
            return Result.retry()
        }

        val taskSpecs = inputData.getStringArray(TaskSpecWorker.DATA_TASK_SPEC_CLASSES)!!.map {
            Class.forName(it).getConstructor().newInstance() as TaskSpec
        }

        val stringInputData = inputData.keyValueMap.mapValues { it.value.toString() }
        val workRequest = TaskSpecWorker.getWorkRequest(
            taskSpecs,
            tag,
            Constraints.Builder().build(),
            stringInputData,
            inputData.getBoolean(TaskSpecWorker.DATA_CELLULAR_ONLY, false)
        )

        workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, workRequest)
        return Result.success()
    }
}
