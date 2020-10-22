package org.odk.collect.audiorecorder.recording

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.odk.collect.audiorecorder.getComponent
import org.odk.collect.audiorecorder.recorder.Recorder
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationChannel = NotificationChannel(
                        "recording_channel",
                        "Recording notifications",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )

                    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(notificationChannel)
                }

                val notification = NotificationCompat.Builder(this, "recording_channel")
                    .setContentTitle("Blah")
                    .setContentText("Blah")
                    .setSmallIcon(applicationInfo.icon)
                    .build()

                startForeground(1, notification)

                if (!recorder.isRecording()) {
                    recorder.start()
                }
            }

            ACTION_CANCEL -> {
                recorder.cancel()
                stopSelf()
            }

            ACTION_STOP -> {
                val file = recorder.stop()
                recordingRepository.create(file)
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_CANCEL = "CANCEL"
    }
}
