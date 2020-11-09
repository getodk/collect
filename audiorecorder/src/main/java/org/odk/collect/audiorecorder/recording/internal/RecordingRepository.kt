package org.odk.collect.audiorecorder.recording.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class RecordingRepository {

    private val _currentSession = MutableLiveData<String?>(null)
    val currentSession: LiveData<String?> = _currentSession

    private val sessions = mutableMapOf<String, MutableLiveData<File?>>()

    fun get(sessionId: String): LiveData<File?> {
        return sessions.getOrPut(sessionId) { MutableLiveData(null) }
    }

    fun start(sessionId: String) {
        _currentSession.value = sessionId
    }

    fun recordingReady(recording: File) {
        _currentSession.value?.let {
            sessions.getOrPut(it) { MutableLiveData(null) }.value = recording
        }

        _currentSession.value = null
    }

    fun clear() {
        _currentSession.value = null

        sessions.values.forEach {
            it.value?.delete()
            it.value = null
        }
    }
}
