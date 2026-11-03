package org.odk.collect.async.workmanager

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.workmanager.TaskSpecWorker.Companion.DATA_NETWORK_CONSTRAINT

class PeriodicTaskSpecWorkerEnqueuerWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val workManager = WorkManager.getInstance(context)
        val tag = inputData.getString(TaskSpecWorker.DATA_TAG) ?: throw IllegalArgumentException()
        val networkConstraint = inputData.getInt(DATA_NETWORK_CONSTRAINT, -1).let {
            if (it != -1) {
                Scheduler.NetworkType.entries[it]
            } else {
                null
            }
        }

        val taskSpecs = inputData.getStringArray(TaskSpecWorker.DATA_TASK_SPEC_CLASSES)!!.map {
            Class.forName(it).getConstructor().newInstance() as TaskSpec
        }

        val stringInputData = inputData.keyValueMap.mapValues { it.value.toString() }
        val workRequest = TaskSpecWorker.getWorkRequest(
            taskSpecs,
            tag,
            stringInputData,
            networkConstraint
        )

        workManager.enqueueUniqueWork("$tag:periodic_step", ExistingWorkPolicy.REPLACE, workRequest)
        return Result.success()
    }
}
