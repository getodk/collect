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

class CoroutineAndWorkManagerScheduler(
    foregroundContext: CoroutineContext,
    backgroundContext: CoroutineContext,
    private val workManager: WorkManager
) : CoroutineScheduler(foregroundContext, backgroundContext) {

    constructor(workManager: WorkManager) : this(
        Dispatchers.Main,
        Dispatchers.IO,
        workManager
    ) // Needed for Java construction

    override fun immediate(
        tag: String,
        spec: TaskSpec,
        inputData: Map<String, String>,
        notificationInfo: NotificationInfo
    ) {
        val workManagerInputData = Data.Builder()
            .putString(TaskSpecWorker.DATA_TASK_SPEC_CLASS, spec.javaClass.name)
            .putBoolean(TaskSpecWorker.FOREGROUND, true)
            .putString(TaskSpecWorker.FOREGROUND_NOTIFICATION_CHANNEL, notificationInfo.channel)
            .putString(
                TaskSpecWorker.FOREGROUND_NOTIFICATION_CHANNEL_NAME,
                notificationInfo.channelName
            )
            .putInt(TaskSpecWorker.FOREGROUND_NOTIFICATION_TITLE, notificationInfo.title)
            .putInt(TaskSpecWorker.FOREGROUND_NOTIFICATION_ID, notificationInfo.id)
            .putAll(inputData)
            .build()

        val workRequest = OneTimeWorkRequest.Builder(TaskSpecWorker::class.java)
            .addTag(tag)
            .setInputData(workManagerInputData)
            .build()
        workManager.beginUniqueWork(tag, ExistingWorkPolicy.REPLACE, workRequest).enqueue()
    }

    override fun networkDeferred(
        tag: String,
        spec: TaskSpec,
        inputData: Map<String, String>,
        networkConstraint: Scheduler.NetworkType?
    ) {
        val constraintNetworkType = when (networkConstraint) {
            Scheduler.NetworkType.WIFI -> NetworkType.UNMETERED
            Scheduler.NetworkType.CELLULAR -> NetworkType.METERED
            else -> NetworkType.CONNECTED
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(constraintNetworkType)
            .build()

        val workManagerInputData = Data.Builder()
            .putString(TaskSpecWorker.DATA_TASK_SPEC_CLASS, spec.javaClass.name)
            .putBoolean(
                TaskSpecWorker.DATA_CELLULAR_ONLY,
                networkConstraint == Scheduler.NetworkType.CELLULAR
            )
            .putAll(inputData)
            .build()

        val workRequest = OneTimeWorkRequest.Builder(TaskSpecWorker::class.java)
            .addTag(tag)
            .setConstraints(constraints)
            .setInputData(workManagerInputData)
            .build()

        workManager.beginUniqueWork(tag, ExistingWorkPolicy.REPLACE, workRequest).enqueue()
    }

    override fun networkDeferredRepeat(
        tag: String,
        spec: TaskSpec,
        repeatPeriod: Long,
        inputData: Map<String, String>
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workManagerInputData = Data.Builder()
            .putString(TaskSpecWorker.DATA_TASK_SPEC_CLASS, spec.javaClass.name)
            .putAll(inputData)
            .build()

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
