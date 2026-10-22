package org.odk.collect.async.workmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.odk.collect.async.TaskSpec

class SchedulerWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val workManager = WorkManager.getInstance(context)
        val specClasses = inputData.getStringArray(DATA_TASK_SPEC_CLASSES)?.toList() ?: emptyList()
        val specs = specClasses.map {
            Class.forName(it).getConstructor().newInstance() as TaskSpec
        }

        TaskSpecWorker.enqueueSpecs(
            workManager,
            specs,
            inputData.keyValueMap.mapValues { it.value.toString() },
            tags.first(),
            Constraints.Builder().build(),
            false
        )

        return Result.success()
    }

    companion object {
        const val DATA_TASK_SPEC_CLASSES = "taskSpecClasses"
    }
}
