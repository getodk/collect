package org.odk.collect.audiorecorder.recording.internal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.R
import org.odk.collect.strings.getLocalizedString

class RecordingNotification(private val service: Service, private val scheduler: Scheduler) {

    private var duration = -1
    private var durationUpdates: Cancellable? = null

    fun show() {
        setupNotificationChannel()
        val notificationIntent = Intent(service, ReturnToAppActivity::class.java)
        val notificationBuilder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL)
            .setContentTitle(service.getLocalizedString(R.string.recording))
            .setContentText("00:00")
            .setSmallIcon(R.drawable.ic_baseline_mic_24)
            .setContentIntent(PendingIntent.getActivity(service, 0, notificationIntent, 0))
            .setPriority(NotificationCompat.PRIORITY_LOW)
        val notification = notificationBuilder
            .build()

        service.startForeground(NOTIFICATION_ID, notification)

        durationUpdates = scheduler.repeat(
            {
                duration += 1
                notificationBuilder.setContentText(LengthFormatter.formatLength(duration.toLong() * 1000))
                (service.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID, notificationBuilder.build())
            },
            1000L
        )
    }

    fun dismiss() {
        durationUpdates?.cancel()
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                service.getLocalizedString(R.string.recording_channel),
                NotificationManager.IMPORTANCE_LOW
            )

            (service.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL = "recording_channel"
    }
}
