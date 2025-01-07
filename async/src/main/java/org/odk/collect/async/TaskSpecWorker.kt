package org.odk.collect.async

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.odk.collect.analytics.Analytics
import org.odk.collect.async.network.ConnectivityProvider

class TaskSpecWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val connectivityProvider: ConnectivityProvider = ConnectivityProvider(context)

    override fun doWork(): Result {
        val cellularOnly = inputData.getBoolean(DATA_CELLULAR_ONLY, false)
        if (cellularOnly && connectivityProvider.currentNetwork != Scheduler.NetworkType.CELLULAR) {
            Analytics.setUserProperty("SawMeteredNonCellular", "true")
            return Result.retry()
        }

        val specClass = inputData.getString(DATA_TASK_SPEC_CLASS)!!
        val spec = Class.forName(specClass).getConstructor().newInstance() as TaskSpec

        val stringInputData = inputData.keyValueMap.mapValues { it.value.toString() }

        try {
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
        } catch (t: Throwable) {
            spec.onException(t)
            return Result.failure()
        }
    }

    private fun isLastUniqueExecution(spec: TaskSpec) =
        spec.maxRetries?.let { runAttemptCount >= it } ?: true

    companion object {
        const val DATA_TASK_SPEC_CLASS = "taskSpecClass"
        const val DATA_CELLULAR_ONLY = "cellularOnly"
    }
}
