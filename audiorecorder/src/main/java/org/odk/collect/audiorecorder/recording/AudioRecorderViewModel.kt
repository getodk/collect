package org.odk.collect.audiorecorder.recording

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java.io.File

abstract class AudioRecorderViewModel : ViewModel() {
    abstract fun isRecording(): LiveData<Boolean>
    abstract fun getRecording(sessionId: String): LiveData<File?>
    abstract fun start(sessionId: String)
    abstract fun stop()
    abstract fun cancel()
}
