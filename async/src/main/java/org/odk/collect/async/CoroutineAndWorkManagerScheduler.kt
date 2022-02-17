package org.odk.collect.async

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class CoroutineAndWorkManagerScheduler(foregroundContext: CoroutineContext, backgroundContext: CoroutineContext, private val workManager: WorkManager) : CoroutineScheduler(foregroundContext, backgroundContext) {

    constructor(workManager: WorkManager) : this(Dispatchers.Main, Dispatchers.IO, workManager) // Needed for Java construction

    override fun networkDeferred(tag: String, spec: TaskSpec, inputData: Map<String, String>) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workManagerInputData = Data.Builder().putAll(inputData).build()

        val worker = spec.getWorkManagerAdapter()
        val workRequest = OneTimeWorkRequest.Builder(worker)
            .addTag(tag)
            .setConstraints(constraints)
            .setInputData(workManagerInputData)
            .build()

        workManager.beginUniqueWork(tag, ExistingWorkPolicy.KEEP, workRequest).enqueue()
    }

    override fun networkDeferred(tag: String, spec: TaskSpec, repeatPeriod: Long, inputData: Map<String, String>) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workManagerInputData = Data.Builder().putAll(inputData).build()

        val worker = spec.getWorkManagerAdapter()
        val builder = PeriodicWorkRequest.Builder(worker, repeatPeriod, TimeUnit.MILLISECONDS)
            .addTag(tag)
            .setInputData(workManagerInputData)
            .setConstraints(constraints)

        spec.backoffPolicy?.let { backoffPolicy ->
            spec.backoffDelay?.let { backoffDelay ->
                builder.setBackoffCriteria(backoffPolicy, backoffDelay, TimeUnit.MILLISECONDS)
            }
        }

        workManager.enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.REPLACE, builder.build())
    }

    override fun cancelDeferred(tag: String) {
        workManager.cancelUniqueWork(tag)
    }

    override fun isDeferredRunning(tag: String): Boolean {
        return isWorkManagerWorkRunning(tag)
    }

    override fun cancelAllDeferred() {
        workManager.cancelAllWork()
    }

    private fun isWorkManagerWorkRunning(tag: String): Boolean {
        val statuses = workManager.getWorkInfosByTag(tag)
        for (workInfo in statuses.get()) {
            if (workInfo.state == WorkInfo.State.RUNNING) {
                return true
            }
        }

        return false
    }
}
