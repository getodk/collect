package org.odk.collect.async.workmanager

import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.TaskSpecScheduler
import org.odk.collect.async.workmanager.TaskSpecWorker.Companion.setup
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
            val workRequest = PeriodicWorkRequest.Builder(
                PeriodicTaskSpecWorkerEnqueuerWorker::class.java,
                repeatPeriod,
                TimeUnit.MILLISECONDS
            ).setup(tag, specs, inputData, cellularOnly, constraints).build()

            workManager.enqueueUniquePeriodicWork(
                tag,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        } else {
            val workRequest = TaskSpecWorker.getWorkRequest(
                specs,
                tag,
                constraints,
                inputData,
                cellularOnly
            )

            workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, workRequest)
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
