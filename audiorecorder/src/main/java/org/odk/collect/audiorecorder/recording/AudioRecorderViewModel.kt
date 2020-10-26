package org.odk.collect.audiorecorder.recording

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.audiorecorder.getComponent
import org.odk.collect.audiorecorder.recording.AudioRecorderService.Companion.ACTION_CANCEL
import org.odk.collect.audiorecorder.recording.AudioRecorderService.Companion.ACTION_START
import org.odk.collect.audiorecorder.recording.AudioRecorderService.Companion.ACTION_STOP
import java.io.File
import javax.inject.Inject

open class AudioRecorderViewModel internal constructor(private val application: Application, private val recordingRepository: RecordingRepository) : ViewModel() {

    val recording: LiveData<File?> = recordingRepository.getRecording()

    open fun start() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_START }
        )
    }

    open fun stop() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_STOP }
        )
    }

    open fun cancel() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_CANCEL }
        )
    }

    open fun endSession() {
        recordingRepository.clear()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {

        @Inject
        internal lateinit var recordingRepository: RecordingRepository

        init {
            application.getComponent().inject(this)
        }

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AudioRecorderViewModel(application, recordingRepository) as T
        }
    }
}
