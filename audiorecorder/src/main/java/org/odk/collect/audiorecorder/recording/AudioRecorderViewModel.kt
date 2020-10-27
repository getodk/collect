package org.odk.collect.audiorecorder.recording

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java.io.File

abstract class AudioRecorderViewModel : ViewModel() {
    abstract val recording: LiveData<File?>
    abstract fun isRecording(): LiveData<Boolean>
    abstract fun start(sessionId: String)
    abstract fun stop()
    abstract fun cancel()
    abstract fun endSession()
}
