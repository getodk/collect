package org.odk.collect.audiorecorder.recording.internal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.R
import org.odk.collect.audiorecorder.getComponent
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.audiorecorder.recorder.Recorder
import org.odk.collect.strings.getLocalizedString
import java.util.Locale
import javax.inject.Inject

class AudioRecorderService : Service() {

    @Inject
    internal lateinit var recorder: Recorder

    @Inject
    internal lateinit var recordingRepository: RecordingRepository

    @Inject
    internal lateinit var scheduler: Scheduler

    private var duration = -1
    private var durationUpdates: Cancellable? = null

    override fun onCreate() {
        super.onCreate()
        getComponent().inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                val output = intent.getSerializableExtra(EXTRA_OUTPUT) as Output

                if (!recorder.isRecording() && sessionId != null) {
                    recordingRepository.start(sessionId)

                    setupNotificationChannel()
                    val notificationIntent = Intent(this, ReturnToAppActivity::class.java)
                    val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                        .setContentTitle(getLocalizedString(R.string.recording))
                        .setContentText("00:00")
                        .setSmallIcon(R.drawable.ic_baseline_mic_24)
                        .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0))
                        .setPriority(PRIORITY_LOW)
                    val notification = notificationBuilder
                        .build()

                    startForeground(NOTIFICATION_ID, notification)
                    durationUpdates = scheduler.repeat(
                        {
                            duration += 1
                            notificationBuilder.setContentText(LengthFormatter.formatLength(duration.toLong() * 1000))
                            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID, notificationBuilder.build())
                        },
                        1000L
                    )

                    recorder.start(output)
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
                NotificationManager.IMPORTANCE_LOW
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
        durationUpdates?.cancel()

        val file = recorder.stop()
        recordingRepository.recordingReady(file)

        stopSelf()
    }

    private fun cleanUp() {
        durationUpdates?.cancel()

        recorder.cancel()
        recordingRepository.clear()
        stopSelf()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL = "recording_channel"

        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_CLEAN_UP = "CLEAN_UP"

        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_OUTPUT = "EXTRA_OUTPUT"
    }
}

object LengthFormatter {
    const val ONE_HOUR = 3600000
    const val ONE_MINUTE = 60000
    const val ONE_SECOND = 1000
    fun formatLength(milliseconds: Long): String {
        val hours = milliseconds / ONE_HOUR
        val minutes = milliseconds % ONE_HOUR / ONE_MINUTE
        val seconds = milliseconds % ONE_MINUTE / ONE_SECOND
        return if (milliseconds < ONE_HOUR) {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}
