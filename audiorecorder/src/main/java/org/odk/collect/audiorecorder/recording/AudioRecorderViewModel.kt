package org.odk.collect.audiorecorder.recording

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.audiorecorder.recorder.Recorder
import java.io.File
import javax.inject.Inject

internal class AudioRecorderViewModel(private val recorder: Recorder, private val recordingRepository: RecordingRepository) : ViewModel() {

    val recording: LiveData<File?> = recordingRepository.getRecording()

    fun start() {
        recorder.start()
    }

    fun stop() {
        val file = recorder.stop()
        recordingRepository.create(file)
    }

    override fun onCleared() {
        recorder.cancel()
    }

    class Factory @Inject constructor(private val recorder: Recorder, private val recordingRepository: RecordingRepository) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AudioRecorderViewModel(recorder, recordingRepository) as T
        }
    }
}
