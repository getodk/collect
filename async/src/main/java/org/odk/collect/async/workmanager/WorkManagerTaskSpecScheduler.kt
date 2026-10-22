package org.odk.collect.async.workmanager

import android.content.Context
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.TaskSpecScheduler
import org.odk.collect.async.workmanager.TaskSpecWorker.Companion.DATA_CELLULAR_ONLY
import org.odk.collect.async.workmanager.TaskSpecWorker.Companion.DATA_TASK_SPEC_CLASS
import java.util.concurrent.TimeUnit

class WorkManagerTaskSpecScheduler(private val workManager: WorkManager) : TaskSpecScheduler {
    override fun schedule(
        tag: String,
        specs: List<TaskSpec>,
        inputData: Map<String, String>,
        networkConstraint: Scheduler.NetworkType?,
        repeatPeriod: Long?
    ) {
        val constraints = getConstraints(networkConstraint)
        val cellularOnly = networkConstraint == Scheduler.NetworkType.CELLULAR

        if (repeatPeriod != null) {
            val workManagerInputData = Data.Builder()
                .putStringArray(
                    SchedulerWorker.DATA_TASK_SPEC_CLASSES,
                    specs.map { it.javaClass.name }.toTypedArray()
                )
                .putBoolean(DATA_CELLULAR_ONLY, cellularOnly)
                .putAll(inputData)
                .build()

            val builder = PeriodicWorkRequest.Builder(
                SchedulerWorker::class.java,
                repeatPeriod,
                TimeUnit.MILLISECONDS
            )
                .addTag(tag)
                .setInputData(workManagerInputData)
                .setConstraints(constraints)

            workManager.enqueueUniquePeriodicWork(
                tag,
                ExistingPeriodicWorkPolicy.REPLACE,
                builder.build()
            )
        } else {
            enqueueSpecs(workManager, specs, inputData, tag, constraints, cellularOnly)
        }
    }

    override fun isRunning(tag: String): Boolean {
        val statuses = workManager.getWorkInfosByTag(tag)
        for (workInfo in statuses.get()) {
            if (workInfo.state == WorkInfo.State.RUNNING) {
                return true
            }
        }

        return false
    }

    override fun cancel(tag: String) {
        workManager.cancelUniqueWork(tag)
    }

    override fun cancelAll() {
        workManager.cancelAllWork()
    }

    private fun getConstraints(networkConstraint: Scheduler.NetworkType?): Constraints {
        val networkRequest = NetworkRequest.Builder().apply {
            addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            when (networkConstraint) {
                Scheduler.NetworkType.WIFI -> addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                Scheduler.NetworkType.CELLULAR -> addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                else -> Unit
            }
        }.build()

        val networkType = when (networkConstraint) {
            Scheduler.NetworkType.WIFI -> NetworkType.UNMETERED
            Scheduler.NetworkType.CELLULAR -> NetworkType.METERED
            else -> NetworkType.CONNECTED
        }

        val constraints = Constraints
            .Builder()
            .setRequiredNetworkRequest(networkRequest, networkType)
            .build()
        return constraints
    }
}

class SchedulerWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val workManager = WorkManager.getInstance(context)
        val specClasses = inputData.getStringArray(DATA_TASK_SPEC_CLASSES)?.toList() ?: emptyList()
        val specs = specClasses.map {
            Class.forName(it).getConstructor().newInstance() as TaskSpec
        }

        enqueueSpecs(
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

private fun enqueueSpecs(
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

        continuation.then(getWorkRequestForTaskSpec(spec, tag, constraints, requestInputData))
    }.enqueue()
}

private fun getWorkRequestForTaskSpec(
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
