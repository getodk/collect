package org.odk.collect.async.workmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.odk.collect.analytics.Analytics
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.network.ConnectivityProvider
import org.odk.collect.async.run
import java.util.concurrent.TimeUnit

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
            is TaskSpec.Result.SUCCESS -> {
                val outputData = Data.Builder().putAll(result.outputData).build()
                Result.success(outputData)
            }

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

        fun getWorkRequestForTaskSpec(
            taskSpec: TaskSpec,
            tag: String,
            constraints: Constraints,
            inputData: Data
        ): OneTimeWorkRequest {
            val builder = OneTimeWorkRequest.Builder(TaskSpecWorker::class.java)
                .addTag(tag)
                .setConstraints(constraints)
                .setInputData(inputData)

            taskSpec.backoffPolicy?.let { backoffPolicy ->
                taskSpec.backoffDelay?.let { backoffDelay ->
                    builder.setBackoffCriteria(
                        backoffPolicy,
                        backoffDelay,
                        TimeUnit.MILLISECONDS
                    )
                }
            }

            return builder.build()
        }

        fun enqueueSpecs(
            workManager: WorkManager,
            specs: List<TaskSpec>,
            inputData: Map<String, String>,
            tag: String,
            constraints: Constraints,
            cellularOnly: Boolean
        ) {
            val firstSpec = specs.first()
            val firstRequestInputData = Data.Builder()
                .putString(DATA_TASK_SPEC_CLASS, firstSpec.javaClass.name)
                .putBoolean(DATA_CELLULAR_ONLY, cellularOnly)
                .putAll(inputData)
                .build()
            val firstWorkRequest =
                getWorkRequestForTaskSpec(firstSpec, tag, constraints, firstRequestInputData)

            specs.drop(1).fold(
                workManager.beginUniqueWork(tag, ExistingWorkPolicy.REPLACE, firstWorkRequest)
            ) { continuation, spec ->
                val requestInputData = Data.Builder()
                    .putString(DATA_TASK_SPEC_CLASS, spec.javaClass.name)
                    .putBoolean(DATA_CELLULAR_ONLY, cellularOnly)
                    .build()

                continuation.then(
                    getWorkRequestForTaskSpec(
                        spec,
                        tag,
                        constraints,
                        requestInputData
                    )
                )
            }.enqueue()
        }
    }
}
