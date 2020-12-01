package org.odk.collect.audiorecorder.testsupport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel
import java.io.File

/**
 * An implementation of audio recorder that always returns the same recording
 */
class StubAudioRecorderViewModel(private val stubRecordingPath: String) : AudioRecorderViewModel() {

    var lastRecording: File? = null
    var lastSession: String? = null

    var wasCleanedUp = false

    private val isRecording = MutableLiveData(false)
    private val recordings = mutableMapOf<String, MutableLiveData<File?>>()

    override fun isRecording(): LiveData<Boolean> {
        return isRecording
    }

    override fun getRecording(sessionId: String): LiveData<File?> {
        return recordings.getOrPut(sessionId) { MutableLiveData(null) }
    }

    override fun start(sessionId: String, output: Output) {
        wasCleanedUp = false
        lastSession = sessionId
        isRecording.value = true
    }

    override fun stop() {
        isRecording.value = false

        val newFile = File.createTempFile("temp", ".m4a")
        File(stubRecordingPath).copyTo(newFile, overwrite = true)
        newFile.deleteOnExit()
        recordings.getOrPut(lastSession!!) { MutableLiveData(null) }.value = newFile

        lastRecording = newFile
    }

    override fun cleanUp() {
        isRecording.value = false
        recordings.values.forEach { it.value = null }
        wasCleanedUp = true
    }
}
