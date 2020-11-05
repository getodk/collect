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
    internal lateinit var recordingSession: RecordingSession

    override fun onCreate() {
        super.onCreate()
        getComponent().inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)

                if (!recorder.isRecording() && sessionId != null) {
                    recordingSession.start(sessionId)

                    setupNotificationChannel()

                    val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                        .setContentTitle(getLocalizedString(R.string.recording))
                        .setSmallIcon(R.drawable.ic_baseline_mic_24)
                        .build()

                    startForeground(NOTIFICATION_ID, notification)

                    recorder.start()
                }
            }

            ACTION_STOP -> {
                stopRecording()
            }

            ACTION_CLEAN_UP -> {
                cleanUp()
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
        cleanUp()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun stopRecording() {
        val file = recorder.stop()
        recordingSession.recordingReady(file)
        stopSelf()
    }

    private fun cleanUp() {
        recorder.cancel()
        recordingSession.end()
        stopSelf()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL = "recording_channel"

        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_CLEAN_UP = "CLEAN_UP"

        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
    }
}
