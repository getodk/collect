package org.odk.collect.audiorecorder.recording.internal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.odk.collect.audiorecorder.R
import org.odk.collect.audiorecorder.getComponent
import org.odk.collect.audiorecorder.recorder.Recorder
import org.odk.collect.strings.getLocalizedString
import javax.inject.Inject

class AudioRecorderService : Service() {

    @Inject
    internal lateinit var recorder: Recorder

    @Inject
    internal lateinit var recordingRepository: RecordingRepository

    override fun onCreate() {
        super.onCreate()
        getComponent().inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                setupNotificationChannel()

                val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                    .setContentTitle(getLocalizedString(R.string.recording))
                    .setSmallIcon(R.drawable.ic_baseline_mic_24)
                    .build()

                startForeground(NOTIFICATION_ID, notification)

                if (!recorder.isRecording()) {
                    recorder.start()
                }
            }

            ACTION_CANCEL -> {
                cancelRecording()
            }

            ACTION_STOP -> {
                stopRecording()
            }
        }

        return START_STICKY
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                getLocalizedString(R.string.recording_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(notificationChannel)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        cancelRecording()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun stopRecording() {
        val file = recorder.stop()
        recordingRepository.create(file)
        stopSelf()
    }

    private fun cancelRecording() {
        recorder.cancel()
        stopSelf()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL = "recording_channel"

        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_CANCEL = "CANCEL"
    }
}
