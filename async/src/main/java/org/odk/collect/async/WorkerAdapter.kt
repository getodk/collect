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
        val completed = spec.getTask(applicationContext, stringInputData, isLastUniqueExecution()).get()

        return if (completed) {
            Result.success()
        } else if (spec.maxRetries == null || runAttemptCount < spec.maxRetries!!) {
            Result.retry()
        } else {
            Result.failure()
        }
    }

    private fun isLastUniqueExecution() = spec.maxRetries?.let { runAttemptCount >= it } ?: true
}
