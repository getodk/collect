package org.odk.collect.audiorecorder.recording.internal

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_CANCEL
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_START
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_STOP
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.EXTRA_SESSION_ID
import java.io.File

internal class RealAudioRecorderViewModel internal constructor(private val application: Application, private val recordingSession: RecordingSession) : AudioRecorderViewModel() {

    private val _isRecording: LiveData<Boolean> = map(recordingSession.get()) { it != null }

    override fun isRecording(): LiveData<Boolean> {
        return _isRecording
    }

    override fun getRecording(sessionId: String): LiveData<File?> {
        return map(recordingSession.get()) {
            if (it != null && it.first == sessionId) {
                it.second
            } else {
                null
            }
        }
    }

    override fun start(sessionId: String) {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SESSION_ID, sessionId)
            }
        )
    }

    override fun stop() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_STOP }
        )
    }

    override fun cancel() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_CANCEL }
        )
    }
}
