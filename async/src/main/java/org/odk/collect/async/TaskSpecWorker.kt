package org.odk.collect.async

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
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

        val foreground = inputData.getBoolean(FOREGROUND, false)
        if (foreground) {
            setupNotificationChannel()
            setForegroundAsync(
                getForegroundInfo(applicationContext, inputData.getInt(FOREGROUND_TYPE, -1))
            )
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

    private fun getForegroundInfo(context: Context, foregroundType: Int): ForegroundInfo {
        val intent = WorkManager.getInstance(context).createCancelPendingIntent(id)

        val notification = NotificationCompat.Builder(applicationContext, "taskSpecWorker")
            .setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            .setContentTitle("Task Spec Worker")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(intent)
            .build()

        return ForegroundInfo(1, notification, foregroundType)
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "taskSpecWorker",
                "TaskSpecWorker",
                NotificationManager.IMPORTANCE_LOW
            )

            (applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(notificationChannel)
        }
    }

    private fun isLastUniqueExecution(spec: TaskSpec) =
        spec.maxRetries?.let { runAttemptCount >= it } ?: true

    companion object {
        const val DATA_TASK_SPEC_CLASS = "taskSpecClass"
        const val DATA_CELLULAR_ONLY = "cellularOnly"

        const val FOREGROUND = "foreground"
        const val FOREGROUND_TYPE = "foreground_type"
    }
}
