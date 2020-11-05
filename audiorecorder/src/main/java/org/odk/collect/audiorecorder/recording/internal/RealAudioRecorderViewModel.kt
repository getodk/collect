package org.odk.collect.audiorecorder.recording.internal

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_CLEAN_UP
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_START
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_STOP
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.EXTRA_SESSION_ID
import java.io.File

internal class RealAudioRecorderViewModel internal constructor(private val application: Application, private val recordingRepository: RecordingRepository) : AudioRecorderViewModel() {

    private val _isRecording: LiveData<Boolean> = map(recordingRepository.currentSession) { it != null }

    override fun isRecording(): LiveData<Boolean> {
        return _isRecording
    }

    override fun getRecording(sessionId: String): LiveData<File?> {
        return recordingRepository.get(sessionId)
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

    override fun cleanUp() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_CLEAN_UP }
        )
    }
}
