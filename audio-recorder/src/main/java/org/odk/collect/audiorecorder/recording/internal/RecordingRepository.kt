package org.odk.collect.audiorecorder.recording.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.audiorecorder.recording.RecordingSession
import java.io.File
import java.io.Serializable

internal class RecordingRepository(appState: AppState) {

    private val _failedToStart =
        appState.get("failedToStart", MutableLiveData<Consumable<Exception?>>(Consumable(null)))
    private val _currentSession =
        appState.get("currentSession", MutableLiveData<RecordingSession?>(null))

    val currentSession: LiveData<RecordingSession?> = _currentSession
    val failedToStart: LiveData<Consumable<Exception?>> = _failedToStart

    fun start(sessionId: Serializable) {
        _currentSession.value = RecordingSession(sessionId, null, 0, 0, false)
        _failedToStart.value = Consumable(null)
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

    fun failToStart(exception: Exception) {
        _currentSession.value = null
        _failedToStart.value = Consumable(exception)
    }
}
