package org.odk.collect.audiorecorder.recording.internal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.R
import org.odk.collect.strings.getLocalizedString

class RecordingNotification(private val service: Service, private val scheduler: Scheduler) {

    private val notificationIntent = Intent(service, ReturnToAppActivity::class.java)
    private val notificationBuilder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL)
        .setContentTitle(service.getLocalizedString(R.string.recording))
        .setContentText(LengthFormatter.formatLength(0))
        .setSmallIcon(R.drawable.ic_baseline_mic_24)
        .setContentIntent(PendingIntent.getActivity(service, 0, notificationIntent, 0))
        .setPriority(NotificationCompat.PRIORITY_LOW)

    fun show() {
        setupNotificationChannel()
        val notification = notificationBuilder
            .build()

        service.startForeground(NOTIFICATION_ID, notification)
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

    fun setDuration(duration: Long) {
        notificationBuilder.setContentText(LengthFormatter.formatLength(duration))
        (service.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL = "recording_channel"
    }
}
