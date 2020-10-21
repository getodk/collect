package org.odk.collect.audiorecorder.recording

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.audiorecorder.recorder.Recorder
import java.io.File
import javax.inject.Inject

internal class AudioRecorderViewModel(private val recorder: Recorder) : ViewModel() {

    private val _recording = MutableLiveData<File?>(null)
    val recording: LiveData<File?> = _recording

    fun start() {
        recorder.start()
    }

    fun stop() {
        val file = recorder.stop()
        _recording.value = file
    }

    override fun onCleared() {
        recorder.cancel()
    }

    class Factory @Inject constructor(private val recorder: Recorder) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AudioRecorderViewModel(recorder) as T
        }
    }
}
