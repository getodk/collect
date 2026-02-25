package org.odk.collect.async.services

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.odk.collect.async.NotificationInfo
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.TaskSpecRunner
import org.odk.collect.async.run
import org.odk.collect.async.services.TaskSpecRunnerService.Companion.EXTRA_INPUT_DATA
import org.odk.collect.async.services.TaskSpecRunnerService.Companion.EXTRA_NOTIFICATION_CHANNEL
import org.odk.collect.async.services.TaskSpecRunnerService.Companion.EXTRA_NOTIFICATION_CHANNEL_NAME
import org.odk.collect.async.services.TaskSpecRunnerService.Companion.EXTRA_NOTIFICATION_ID
import org.odk.collect.async.services.TaskSpecRunnerService.Companion.EXTRA_NOTIFICATION_TITLE
import org.odk.collect.async.services.TaskSpecRunnerService.Companion.EXTRA_SPEC_CLASS
import org.odk.collect.icons.R

class ForegroundServiceTaskSpecRunner(private val application: Application) : TaskSpecRunner {
    override fun run(
        tag: String,
        taskSpec: TaskSpec,
        inputData: Map<String, String>,
        notificationInfo: NotificationInfo
    ) {
        val intent = Intent(application, TaskSpecRunnerService::class.java).also {
            it.putExtra(EXTRA_NOTIFICATION_ID, notificationInfo.id)
            it.putExtra(EXTRA_NOTIFICATION_CHANNEL, notificationInfo.channel)
            it.putExtra(EXTRA_NOTIFICATION_CHANNEL_NAME, notificationInfo.channelName)
            it.putExtra(EXTRA_NOTIFICATION_TITLE, application.getString(notificationInfo.title))
            it.putExtra(EXTRA_SPEC_CLASS, taskSpec.javaClass.name)
            it.putExtra(EXTRA_INPUT_DATA, HashMap(inputData))
        }

        application.startService(intent)
    }
}

class TaskSpecRunnerService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val notificationChannel = intent!!.getStringExtra(EXTRA_NOTIFICATION_CHANNEL)!!
        val notificationChannelName = intent.getStringExtra(EXTRA_NOTIFICATION_CHANNEL_NAME)!!
        setupNotificationChannel(notificationChannel, notificationChannelName)

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val notificationTitle = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE)
        val notification = NotificationCompat.Builder(applicationContext, notificationChannel)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentTitle(notificationTitle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        ServiceCompat.startForeground(
            this,
            notificationId,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )

        val inputData = intent.getSerializableExtra(EXTRA_INPUT_DATA) as HashMap<String, String>
        val taskSpec = Class
            .forName(intent.getStringExtra(EXTRA_SPEC_CLASS)!!)
            .getConstructor()
            .newInstance() as TaskSpec

        CoroutineScope(Dispatchers.IO).launch {
            try {
                taskSpec.run(application, inputData, 1, true, { false })
            } finally {
                stopSelf()
            }
        }

        return START_NOT_STICKY
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

        (applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(notificationChannel)
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notificationId"
        const val EXTRA_NOTIFICATION_CHANNEL = "notificationChannel"
        const val EXTRA_NOTIFICATION_CHANNEL_NAME = "notificationChannelName"
        const val EXTRA_NOTIFICATION_TITLE = "notificationChannelTitle"
        const val EXTRA_SPEC_CLASS = "specClass"
        const val EXTRA_INPUT_DATA = "inputData"
    }
}
