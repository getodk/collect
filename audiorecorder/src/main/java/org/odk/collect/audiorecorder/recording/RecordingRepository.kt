package org.odk.collect.audiorecorder.recording

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class RecordingRepository {

    private val _recording = MutableLiveData<File?>(null)

    fun create(recording: File) {
        _recording.value = recording
    }

    fun getRecording(): LiveData<File?> {
        return _recording
    }

    fun clear() {
        _recording.value = null
    }
}
