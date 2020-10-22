package org.odk.collect.audiorecorder.recording

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.audiorecorder.recording.AudioRecorderService.Companion.ACTION_CANCEL
import org.odk.collect.audiorecorder.recording.AudioRecorderService.Companion.ACTION_START
import org.odk.collect.audiorecorder.recording.AudioRecorderService.Companion.ACTION_STOP
import java.io.File
import javax.inject.Inject

internal class AudioRecorderViewModel(private val application: Application, private val recordingRepository: RecordingRepository) : ViewModel() {

    val recording: LiveData<File?> = recordingRepository.getRecording()

    fun start() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_START }
        )
    }

    fun stop() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_STOP }
        )
    }

    fun cancel() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_CANCEL }
        )
    }

    fun endSession() {
        recordingRepository.clear()
    }

    class Factory @Inject constructor(private val application: Application, private val recordingRepository: RecordingRepository) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AudioRecorderViewModel(application, recordingRepository) as T
        }
    }
}
