package org.odk.collect.audiorecorder.support

import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.audiorecorder.recorder.Recorder
import java.io.File

class FakeRecorder : Recorder {

    var file: File? = null
    lateinit var output: Output

    private val _recordings = mutableListOf<Unit>()
    val recordings: List<Unit> = _recordings

    private var recording = false
    private var cancelled = false

    override fun isRecording(): Boolean {
        return recording
    }

    fun wasCancelled(): Boolean {
        return cancelled
    }

    override fun start(output: Output) {
        recording = true
        cancelled = false
        this.output = output
        _recordings.add(Unit)
    }

    override fun stop(): File {
        recording = false
        val newFile = File.createTempFile("recording", ".mp3")
        file = newFile
        return newFile
    }

    override fun cancel() {
        recording = false
        cancelled = true
    }
}
