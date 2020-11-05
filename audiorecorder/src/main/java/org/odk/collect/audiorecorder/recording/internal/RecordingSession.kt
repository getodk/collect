package org.odk.collect.audiorecorder.recording.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class RecordingSession {

    private val _state = MutableLiveData<Pair<String, File?>?>(null)

    fun get(): LiveData<Pair<String, File?>?> {
        return _state
    }

    fun start(sessionId: String) {
        _state.value = Pair(sessionId, null)
    }

    fun recordingReady(recording: File) {
        _state.value?.let {
            _state.value = it.copy(second = recording)
        }
    }

    fun end() {
        _state.value?.let {
            _state.value = null
        }
    }
}
