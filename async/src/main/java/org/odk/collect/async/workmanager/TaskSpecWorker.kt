package org.odk.collect.async.workmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class TaskSpecWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    private var isStopped = false

    private val stringInputData: Map<String, String> by lazy {
        inputData.keyValueMap.mapValues { it.value.toString() }
    }

    private val connectivityProvider: ConnectivityProvider = ConnectivityProvider(context)

    override fun doWork(): Result {
        val tag = tags.first()
        return ChainLocks.withLock(tag) { lockAcquired ->
            if (lockAcquired) {
                val cellularOnly = inputData.getBoolean(DATA_CELLULAR_ONLY, false)
                if (!cellularOnly || connectivityProvider.currentNetwork == Scheduler.NetworkType.CELLULAR) {
                    val taskSpecs = inputData.getStringArray(DATA_TASK_SPEC_CLASSES)!!.map {
                        Class.forName(it).getConstructor().newInstance() as TaskSpec
                    }

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

                    if (nextWorkRequest is OneTimeWorkRequest) {
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
            } else {
                Result.retry()
            }
        }
    }

    private fun getNextWorkRequest(
        result: RunResult,
        taskSpecs: List<TaskSpec>,
        tag: String,
        cellularOnly: Boolean
    ): WorkRequest? {
        val outputData = result.outputData
        return if (taskSpecs.size > 1 && outputData != null) {
            getWorkRequest(
                taskSpecs.drop(1),
                tag,
                Constraints.Builder().build(),
                outputData,
                null,
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
            repeatPeriod: Long?,
            cellularOnly: Boolean
        ): WorkRequest {
            val builder = if (repeatPeriod != null) {
                PeriodicWorkRequest.Builder(
                    TaskSpecWorker::class.java,
                    repeatPeriod,
                    TimeUnit.MILLISECONDS
                )
            } else {
                OneTimeWorkRequest.Builder(TaskSpecWorker::class.java)
            }

            builder.addTag(tag)
                .setInputData(getInputData(specs, inputData, cellularOnly))
                .setConstraints(constraints)

            val firstSpec = specs.first()
            firstSpec.backoffPolicy?.let { backoffPolicy ->
                firstSpec.backoffDelay?.let { backoffDelay ->
                    builder.setBackoffCriteria(
                        backoffPolicy,
                        backoffDelay,
                        TimeUnit.MILLISECONDS
                    )
                }
            }

            return builder.build()
        }

        private fun getInputData(
            specs: List<TaskSpec>,
            inputData: Map<String, String>,
            cellularOnly: Boolean
        ): Data {
            return Data.Builder()
                .putStringArray(
                    DATA_TASK_SPEC_CLASSES,
                    specs.map { it.javaClass.name }.toTypedArray()
                )
                .putBoolean(DATA_CELLULAR_ONLY, cellularOnly)
                .putAll(inputData)
                .build()
        }
    }
}

private object ChainLocks {
    private val locks = ConcurrentHashMap<String, ReentrantLock>()

    fun <T> withLock(tag: String, function: (lockAcquired: Boolean) -> T): T {
        val lock = locks.computeIfAbsent(tag) { ReentrantLock() }
        val acquired = lock.tryLock()
        return try {
            function(acquired)
        } finally {
            if (acquired) {
                lock.unlock()
            }
        }
    }
}
