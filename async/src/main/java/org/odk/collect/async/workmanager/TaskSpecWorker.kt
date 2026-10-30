package org.odk.collect.async.workmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.odk.collect.analytics.Analytics
import org.odk.collect.async.RunResult
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.network.ConnectivityProvider
import org.odk.collect.async.run
import java.util.concurrent.TimeUnit

class TaskSpecWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    private var isStopped = false

    private val connectivityProvider: ConnectivityProvider = ConnectivityProvider(context)

    override fun doWork(): Result {
        val tag = tags.first()
        val cellularOnly = inputData.getBoolean(DATA_CELLULAR_ONLY, false)
        return if (!cellularOnly || connectivityProvider.currentNetwork == Scheduler.NetworkType.CELLULAR) {
            val taskSpecs = inputData.getStringArray(DATA_TASK_SPEC_CLASSES)!!.map {
                Class.forName(it).getConstructor().newInstance() as TaskSpec
            }

            val stringInputData = inputData.keyValueMap.mapValues { it.value.toString() }
            val result = taskSpecs.first().run(
                applicationContext,
                stringInputData,
                runAttemptCount,
                false,
                { isStopped }
            )

            val nextWorkRequest = getNextWorkRequest(
                result,
                taskSpecs,
                tag,
                cellularOnly
            )

            if (nextWorkRequest != null) {
                WorkManager.getInstance(context)
                    .enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, nextWorkRequest)
            }

            when (result) {
                is RunResult.Success -> Result.success()
                is RunResult.Retry -> Result.retry()
                is RunResult.Failure -> Result.failure()
                RunResult.Error -> Result.failure()
            }
        } else {
            Analytics.setUserProperty("SawMeteredNonCellular", "true")
            Result.retry()
        }
    }

    private fun getNextWorkRequest(
        result: RunResult,
        taskSpecs: List<TaskSpec>,
        tag: String,
        cellularOnly: Boolean
    ): OneTimeWorkRequest? {
        val outputData = result.outputData
        return if (taskSpecs.size > 1 && outputData != null) {
            getWorkRequest(
                taskSpecs.drop(1),
                tag,
                Constraints.Builder().build(),
                outputData,
                cellularOnly
            )
        } else {
            null
        }
    }

    override fun onStopped() {
        super.onStopped()
        isStopped = true
    }

    companion object {
        const val DATA_TASK_SPEC_CLASSES = "taskSpecClasses"
        const val DATA_CELLULAR_ONLY = "cellularOnly"

        fun getWorkRequest(
            specs: List<TaskSpec>,
            tag: String,
            constraints: Constraints,
            inputData: Map<String, String>,
            cellularOnly: Boolean
        ): OneTimeWorkRequest {
            val builder = OneTimeWorkRequest.Builder(TaskSpecWorker::class.java)
            return builder.setup(tag, specs, inputData, cellularOnly, constraints).build()
        }

        fun <T : WorkRequest.Builder<T, *>, U : WorkRequest> WorkRequest.Builder<T, U>.setup(
            tag: String,
            specs: List<TaskSpec>,
            inputData: Map<String, String>,
            cellularOnly: Boolean,
            constraints: Constraints
        ): WorkRequest.Builder<T, U> {
            this.addTag(tag)
                .setInputData(getInputData(specs, inputData, cellularOnly))
                .setConstraints(constraints)

            val firstSpec = specs.first()
            firstSpec.backoffPolicy?.let { backoffPolicy ->
                firstSpec.backoffDelay?.let { backoffDelay ->
                    this.setBackoffCriteria(
                        backoffPolicy,
                        backoffDelay,
                        TimeUnit.MILLISECONDS
                    )
                }
            }

            return this
        }

        private fun getInputData(
            specs: List<TaskSpec>,
            inputData: Map<String, String>,
            cellularOnly: Boolean
        ): Data {
            return Data.Builder()
                .putAll(inputData)
                .putStringArray(
                    DATA_TASK_SPEC_CLASSES,
                    specs.map { it.javaClass.name }.toTypedArray()
                )
                .putBoolean(DATA_CELLULAR_ONLY, cellularOnly)
                .build()
        }
    }
}
