package org.odk.collect.async.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.odk.collect.analytics.Analytics
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.network.ConnectivityProvider
import org.odk.collect.async.run

class TaskSpecWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    private var isStopped = false

    private val taskSpec: TaskSpec by lazy {
        Class
            .forName(inputData.getString(DATA_TASK_SPEC_CLASS)!!)
            .getConstructor()
            .newInstance() as TaskSpec
    }

    private val stringInputData: Map<String, String> by lazy {
        inputData.keyValueMap.mapValues { it.value.toString() }
    }

    private val connectivityProvider: ConnectivityProvider = ConnectivityProvider(context)

    override fun doWork(): Result {
        val cellularOnly = inputData.getBoolean(DATA_CELLULAR_ONLY, false)
        if (cellularOnly && connectivityProvider.currentNetwork != Scheduler.NetworkType.CELLULAR) {
            Analytics.Companion.setUserProperty("SawMeteredNonCellular", "true")
            return Result.retry()
        }

        val result = taskSpec.run(
            applicationContext,
            stringInputData,
            runAttemptCount,
            false,
            { isStopped }
        )

        return when (result) {
            TaskSpec.Result.SUCCESS -> Result.success()
            TaskSpec.Result.FAILURE -> Result.failure()
            TaskSpec.Result.RETRY -> Result.retry()
        }
    }

    override fun onStopped() {
        super.onStopped()
        isStopped = true
    }

    companion object {
        const val DATA_TASK_SPEC_CLASS = "taskSpecClass"
        const val DATA_CELLULAR_ONLY = "cellularOnly"
    }
}
