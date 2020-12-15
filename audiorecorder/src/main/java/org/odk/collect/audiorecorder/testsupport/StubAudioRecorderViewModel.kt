package org.odk.collect.audiorecorder.testsupport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel
import org.odk.collect.audiorecorder.recording.RecordingSession
import java.io.File

/**
 * An implementation of audio recorder that always returns the same recording
 */
class StubAudioRecorderViewModel(private val stubRecordingPath: String) : AudioRecorderViewModel() {

    var lastRecording: File? = null
    var lastSession: String? = null
    var duration: Int = 0
        set(value) {
            field = value
            currentSession.value?.let {
                currentSession.value = it.copy(duration = value.toLong())
            }
        }

    var wasCleanedUp = false

    private var isRecording = false
    private var failOnStart = false
    private val currentSession = MutableLiveData<RecordingSession>(null)

    override fun isRecording(): Boolean {
        return isRecording
    }

    override fun getCurrentSession(): LiveData<RecordingSession?> {
        return currentSession
    }

    override fun start(sessionId: String, output: Output) {
        if (failOnStart) {
            currentSession.value = RecordingSession(sessionId, null, 0, 0, paused = false, failedToStart = Exception())
        } else {
            wasCleanedUp = false
            lastSession = sessionId
            isRecording = true
            currentSession.value = RecordingSession(sessionId, null, 0, 0, false)
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

        val newFile = File.createTempFile("temp", ".m4a")
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
