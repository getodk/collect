package org.odk.collect.audiorecorder.recording

import android.app.Service
import android.content.Intent
import android.os.IBinder
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
        applicationContext.getComponent().inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> recorder.start()
            ACTION_CANCEL -> recorder.cancel()

            ACTION_STOP -> {
                val file = recorder.stop()
                recordingRepository.create(file)
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
