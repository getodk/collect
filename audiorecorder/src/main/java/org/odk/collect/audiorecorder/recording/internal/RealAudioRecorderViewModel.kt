package org.odk.collect.audiorecorder.recording.internal

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_CANCEL
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_START
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_STOP
import java.io.File

internal class RealAudioRecorderViewModel internal constructor(private val application: Application, private val recordingRepository: RecordingRepository) : AudioRecorderViewModel() {

    override val recording: LiveData<File?> = recordingRepository.getRecording()

    override fun start() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_START }
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

    override fun endSession() {
        recordingRepository.clear()
    }
}
