package org.odk.collect.async

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
import java.util.concurrent.TimeUnit

class WorkManagerTaskSpecScheduler(private val workManager: WorkManager) : TaskSpecScheduler {
    override fun schedule(
        tag: String,
        spec: TaskSpec,
        inputData: Map<String, String>,
        networkConstraint: Scheduler.NetworkType?,
        repeatPeriod: Long?
    ) {
        val constraints = getConstraints(networkConstraint)
        val workManagerInputData = Data.Builder()
            .putString(TaskSpecWorker.DATA_TASK_SPEC_CLASS, spec.javaClass.name)
            .putBoolean(
                TaskSpecWorker.DATA_CELLULAR_ONLY,
                networkConstraint == Scheduler.NetworkType.CELLULAR
            )
            .putAll(inputData)
            .build()

        if (repeatPeriod != null) {
            val builder = PeriodicWorkRequest.Builder(
                TaskSpecWorker::class.java,
                repeatPeriod,
                TimeUnit.MILLISECONDS
            )
                .addTag(tag)
                .setInputData(workManagerInputData)
                .setConstraints(constraints)

            spec.backoffPolicy?.let { backoffPolicy ->
                spec.backoffDelay?.let { backoffDelay ->
                    builder.setBackoffCriteria(backoffPolicy, backoffDelay, TimeUnit.MILLISECONDS)
                }
            }

            workManager.enqueueUniquePeriodicWork(
                tag,
                ExistingPeriodicWorkPolicy.REPLACE,
                builder.build()
            )
        } else {
            val workRequest = OneTimeWorkRequest.Builder(TaskSpecWorker::class.java)
                .addTag(tag)
                .setConstraints(constraints)
                .setInputData(workManagerInputData)
                .build()

            workManager.beginUniqueWork(tag, ExistingWorkPolicy.REPLACE, workRequest).enqueue()
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
