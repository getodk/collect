package org.odk.collect.async

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class TaskSpecWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val specClass = inputData.getString(TASK_SPEC_CLASS)!!
        val spec = Class.forName(specClass).getConstructor().newInstance() as TaskSpec

        val stringInputData = inputData.keyValueMap.mapValues { it.value.toString() }
        val completed =
            spec.getTask(applicationContext, stringInputData, isLastUniqueExecution(spec)).get()
        val maxRetries = spec.maxRetries

        return if (completed) {
            Result.success()
        } else if (maxRetries == null || runAttemptCount < maxRetries) {
            Result.retry()
        } else {
            Result.failure()
        }
    }

    private fun isLastUniqueExecution(spec: TaskSpec) =
        spec.maxRetries?.let { runAttemptCount >= it } ?: true

    companion object {
        const val TASK_SPEC_CLASS = "taskSpecClass"
    }
}
