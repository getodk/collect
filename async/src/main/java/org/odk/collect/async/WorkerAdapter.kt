package org.odk.collect.async

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

abstract class WorkerAdapter(
    private val spec: TaskSpec,
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val stringInputData = inputData.keyValueMap.mapValues { it.value.toString() }
            .toMutableMap()
            .apply {
                this[TaskSpec.DATA_LAST_UNIQUE_EXECUTION] = (runAttemptCount >= spec.maxRetries).toString()
            }
        val completed = spec.getTask(applicationContext, stringInputData).get()

        return if (completed) {
            Result.success()
        } else if (spec.maxRetries == TaskSpec.UNLIMITED_NUMBER_OF_RETRIES || runAttemptCount < spec.maxRetries) {
            Result.retry()
        } else {
            Result.failure()
        }
    }
}
