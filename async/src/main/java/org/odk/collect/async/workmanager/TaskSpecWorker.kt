package org.odk.collect.async.workmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.odk.collect.analytics.Analytics
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.network.ConnectivityProvider
import org.odk.collect.async.run
import org.odk.collect.icons.R

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

        val isForeground = inputData.getBoolean(FOREGROUND, false)
        if (isForeground) {
            val notificationChannel = inputData.getString(FOREGROUND_NOTIFICATION_CHANNEL)!!
            val notificationChannelName =
                inputData.getString(FOREGROUND_NOTIFICATION_CHANNEL_NAME)!!
            setupNotificationChannel(notificationChannel, notificationChannelName)

            val notificationTitle = inputData.getInt(FOREGROUND_NOTIFICATION_TITLE, -1)
            setForegroundAsync(
                getForegroundInfo(
                    applicationContext,
                    notificationChannel,
                    notificationTitle,
                    inputData.getInt(FOREGROUND_NOTIFICATION_ID, -1)
                )
            )
        }

        val result = taskSpec.run(
            applicationContext,
            stringInputData,
            runAttemptCount,
            isForeground,
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

    private fun getForegroundInfo(
        context: Context,
        notificationChannel: String,
        @StringRes notificationTitle: Int,
        notificationId: Int
    ): ForegroundInfo {
        val intent = WorkManager.Companion.getInstance(context).createCancelPendingIntent(id)

        val notification = NotificationCompat.Builder(applicationContext, notificationChannel)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(context.getString(notificationTitle))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(intent)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    private fun setupNotificationChannel(
        notificationChannel: String,
        notificationChannelName: String
    ) {
        val notificationChannel = NotificationChannel(
            notificationChannel,
            notificationChannelName,
            NotificationManager.IMPORTANCE_LOW
        )

        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(notificationChannel)
    }

    companion object {
        const val DATA_TASK_SPEC_CLASS = "taskSpecClass"
        const val DATA_CELLULAR_ONLY = "cellularOnly"

        const val FOREGROUND = "foreground"
        const val FOREGROUND_NOTIFICATION_CHANNEL = "notification_channel"
        const val FOREGROUND_NOTIFICATION_CHANNEL_NAME = "notification_channel_name"
        const val FOREGROUND_NOTIFICATION_TITLE = "notification_title"
        const val FOREGROUND_NOTIFICATION_ID = "notification_id"
    }
}
