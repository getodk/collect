package org.odk.collect.async.workmanager

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.workmanager.TaskSpecWorker.Companion.DATA_TAG

class PeriodicTaskSpecWorkerEnqueuerWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val workManager = WorkManager.getInstance(context)
        val tag = inputData.getString(DATA_TAG) ?: throw IllegalArgumentException()

        val taskSpecs = inputData.getStringArray(TaskSpecWorker.DATA_TASK_SPEC_CLASSES)!!.map {
            Class.forName(it).getConstructor().newInstance() as TaskSpec
        }

        val stringInputData = inputData.keyValueMap.mapValues { it.value.toString() }
        val workRequest = TaskSpecWorker.getWorkRequest(
            taskSpecs,
            tag,
            stringInputData,
            inputData.getBoolean(TaskSpecWorker.DATA_CELLULAR_ONLY, false)
        )

        workManager.enqueueUniqueWork("$tag:periodic_step", ExistingWorkPolicy.REPLACE, workRequest)
        return Result.success()
    }
}
