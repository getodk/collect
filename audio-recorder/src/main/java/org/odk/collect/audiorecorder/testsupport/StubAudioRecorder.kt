package org.odk.collect.audiorecorder.testsupport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.audiorecorder.recording.AudioRecorder
import org.odk.collect.audiorecorder.recording.RecordingSession
import java.io.File
import java.io.Serializable

/**
 * An implementation of audio recorder that always returns the same recording
 */
class StubAudioRecorder(private val stubRecordingPath: String) : AudioRecorder() {

    var lastRecording: File? = null
    var lastSession: Serializable? = null

    var duration: Int = 0
        set(value) {
            field = value
            currentSession.value?.let {
                currentSession.value = it.copy(duration = value.toLong())
            }
        }
    var amplitude: Int = 0
        set(value) {
            field = value
            currentSession.value?.let {
                currentSession.value = it.copy(amplitude = value)
            }
        }

    var wasCleanedUp = false

    private var isRecording = false
    private var failOnStart = false
    private val currentSession = MutableLiveData<RecordingSession?>(null)
    private val failedToStart = MutableLiveData<Consumable<Exception?>>(Consumable(null))

    override fun isRecording(): Boolean {
        return isRecording
    }

    override fun getCurrentSession(): LiveData<RecordingSession?> {
        return currentSession
    }

    override fun failedToStart(): LiveData<Consumable<java.lang.Exception?>> {
        return failedToStart
    }

    override fun start(sessionId: Serializable, output: Output) {
        if (!isRecording) {
            if (failOnStart) {
                currentSession.value = null
                failedToStart.value = Consumable(Exception())
            } else {
                wasCleanedUp = false
                lastSession = sessionId
                isRecording = true
                currentSession.value = RecordingSession(sessionId, null, 0, 0, false)
            }
        }
    }

    override fun pause() {
        currentSession.value?.let {
            currentSession.value = it.copy(paused = true)
        }
    }

    override fun resume() {
        currentSession.value?.let {
            currentSession.value = it.copy(paused = false)
        }
    }

    override fun stop() {
        isRecording = false

        val newFile = File.createTempFile("temp", ".fake")
        File(stubRecordingPath).copyTo(newFile, overwrite = true)
        newFile.deleteOnExit()
        currentSession.value?.let {
            currentSession.value = it.copy(file = newFile, paused = false)
        }

        lastRecording = newFile
    }

    override fun cleanUp() {
        isRecording = false

        currentSession.value = null
        wasCleanedUp = true
    }

    fun failOnStart() {
        failOnStart = true
    }
}
