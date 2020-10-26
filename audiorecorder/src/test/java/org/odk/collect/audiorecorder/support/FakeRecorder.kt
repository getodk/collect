package org.odk.collect.audiorecorder.support

import org.odk.collect.audiorecorder.recorder.Recorder
import java.io.File

class FakeRecorder : Recorder {

    val file: File = File.createTempFile("recording", ".mp3")

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

    override fun start() {
        recording = true
        cancelled = false
        _recordings.add(Unit)
    }

    override fun stop(): File {
        recording = false
        return file
    }

    override fun cancel() {
        recording = false
        cancelled = true
    }
}
