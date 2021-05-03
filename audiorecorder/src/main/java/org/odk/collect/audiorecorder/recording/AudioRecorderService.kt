package org.odk.collect.audiorecorder.recording

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.AudioRecorderDependencyComponentProvider
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.audiorecorder.recorder.Recorder
import org.odk.collect.audiorecorder.recording.internal.RecordingForegroundServiceNotification
import org.odk.collect.audiorecorder.recording.internal.RecordingRepository
import java.io.Serializable
import javax.inject.Inject

class AudioRecorderService : Service() {

    @Inject
    internal lateinit var recorder: Recorder

    @Inject
    internal lateinit var recordingRepository: RecordingRepository

    @Inject
    internal lateinit var scheduler: Scheduler

    private lateinit var notification: RecordingForegroundServiceNotification
    private var duration = 0L
    private var durationUpdates: Cancellable? = null
    private var amplitudeUpdates: Cancellable? = null

    override fun onCreate() {
        super.onCreate()
        val provider = applicationContext as AudioRecorderDependencyComponentProvider
        provider.audioRecorderDependencyComponent.inject(this)

        notification = RecordingForegroundServiceNotification(this, recordingRepository)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getSerializableExtra(EXTRA_SESSION_ID)
                val output = intent.getSerializableExtra(EXTRA_OUTPUT) as Output

                if (!recorder.isRecording() && sessionId != null) {
                    startRecording(sessionId, output)
                }
            }

            ACTION_PAUSE -> {
                if (recorder.isRecording()) {
                    recorder.pause()
                    recordingRepository.setPaused(true)

                    stopUpdates()
                }
            }

            ACTION_RESUME -> {
                if (recorder.isRecording()) {
                    recorder.resume()
                    recordingRepository.setPaused(false)

                    startUpdates()
                }
            }

            ACTION_STOP -> {
                if (recorder.isRecording()) {
                    stopRecording()
                }
            }

            ACTION_CLEAN_UP -> {
                cleanUp()
            }
        }

        return START_NOT_STICKY
    }

    private fun startRecording(sessionId: Serializable, output: Output) {
        notification.show()

        try {
            recorder.start(output)
            recordingRepository.start(sessionId)
            startUpdates()
        } catch (e: Exception) {
            notification.dismiss()
            recordingRepository.failToStart(e)
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
        stopUpdates()
        notification.dismiss()

        val file = recorder.stop()
        recordingRepository.recordingReady(file)
    }

    private fun cleanUp() {
        stopUpdates()
        notification.dismiss()

        recorder.cancel()
        recordingRepository.clear()
    }

    private fun startUpdates() {
        durationUpdates = scheduler.repeat(
            {
                recordingRepository.setDuration(duration)
                duration += 1000
            },
            1000L
        )

        amplitudeUpdates = scheduler.repeat({ recordingRepository.setAmplitude(recorder.amplitude) }, 100L)
    }

    private fun stopUpdates() {
        amplitudeUpdates?.cancel()
        durationUpdates?.cancel()
    }

    companion object {
        const val ACTION_START = "START"
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_RESUME = "RESUME"
        const val ACTION_STOP = "STOP"
        const val ACTION_CLEAN_UP = "CLEAN_UP"

        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_OUTPUT = "EXTRA_OUTPUT"
    }
}
