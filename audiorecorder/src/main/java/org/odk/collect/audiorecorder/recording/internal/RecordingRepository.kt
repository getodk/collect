package org.odk.collect.audiorecorder.recording.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.audiorecorder.recording.RecordingSession
import java.io.File
import java.io.Serializable
import java.lang.Exception

internal class RecordingRepository {

    private val _currentSession = MutableLiveData<RecordingSession?>(null)
    val currentSession: LiveData<RecordingSession?> = _currentSession

    fun start(sessionId: Serializable) {
        _currentSession.value = RecordingSession(sessionId, null, 0, 0, false)
    }

    fun setDuration(duration: Long) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(duration = duration)
        }
    }

    fun setAmplitude(amplitude: Int) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(amplitude = amplitude)
        }
    }

    fun setPaused(paused: Boolean) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(paused = paused)
        }
    }

    fun recordingReady(recording: File) {
        _currentSession.value?.let {
            _currentSession.value = it.copy(file = recording, paused = false)
        }
    }

    fun clear() {
        _currentSession.value = null
    }

    fun failToStart(sessionId: Serializable, exception: Exception) {
        _currentSession.value = RecordingSession(sessionId, null, 0, 0, paused = false, failedToStart = exception)
    }
}
