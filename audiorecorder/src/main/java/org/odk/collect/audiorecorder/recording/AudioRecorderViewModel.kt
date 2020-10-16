package org.odk.collect.audiorecorder.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.audiorecorder.recorder.Recorder
import java.io.File
import javax.inject.Inject

internal class AudioRecorderViewModel(private val recorder: Recorder) : ViewModel() {

    fun start() {
        recorder.start()
    }

    fun stop(): File {
        return recorder.stop()
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