package org.odk.collect.audiorecorder.recording.internal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import org.odk.collect.androidshared.ui.ReturnToAppActivity
import org.odk.collect.androidshared.utils.RuntimeUniqueIdGenerator
import org.odk.collect.audiorecorder.recording.RecordingSession
import org.odk.collect.strings.format.formatLength
import org.odk.collect.strings.localization.getLocalizedString

internal class RecordingForegroundServiceNotification(private val service: Service, private val recordingRepository: RecordingRepository) {

    private val notificationIntent = Intent(service, ReturnToAppActivity::class.java)
    private val notificationBuilder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL)
        .setContentTitle(service.getLocalizedString(org.odk.collect.strings.R.string.recording))
        .setContentText(formatLength(0))
        .setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
        .setContentIntent(PendingIntent.getActivity(service, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE))
        .setPriority(NotificationCompat.PRIORITY_LOW)

    private val notificationManager = (service.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)

    private val sessionObserver = Observer<RecordingSession?> {
        if (it != null) {
            notificationBuilder.setContentText(formatLength(it.duration))
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    fun show() {
        setupNotificationChannel()
        val notification = notificationBuilder
            .build()

        service.startForeground(NOTIFICATION_ID, notification)
        recordingRepository.currentSession.observeForever(sessionObserver)
    }

    fun dismiss() {
        recordingRepository.currentSession.removeObserver(sessionObserver)
        service.stopSelf()
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                service.getLocalizedString(org.odk.collect.strings.R.string.recording_channel),
                NotificationManager.IMPORTANCE_LOW
            )

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private val NOTIFICATION_ID = RuntimeUniqueIdGenerator.nextInt()
        private const val NOTIFICATION_CHANNEL = "recording_channel"
    }
}
