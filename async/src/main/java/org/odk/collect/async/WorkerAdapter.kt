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
                this[TaskSpec.LAST_UNIQUE_ATTEMPT] = (runAttemptCount >= spec.numberOfRetries).toString()
            }
        val completed = spec.getTask(applicationContext, stringInputData).get()

        return if (completed) {
            Result.success()
        } else if (spec.numberOfRetries == -1 || runAttemptCount <= spec.numberOfRetries) {
            Result.retry()
        } else {
            Result.failure()
        }
    }
}
