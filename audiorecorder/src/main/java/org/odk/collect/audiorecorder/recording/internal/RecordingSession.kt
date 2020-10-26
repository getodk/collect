package org.odk.collect.audiorecorder.recording.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class RecordingSession {

    private val _recording = MutableLiveData<File?>(null)

    fun getRecording(): LiveData<File?> {
        return _recording
    }

    fun recordingReady(recording: File) {
        _recording.value = recording
    }

    fun finish() {
        _recording.value = null
    }
}
