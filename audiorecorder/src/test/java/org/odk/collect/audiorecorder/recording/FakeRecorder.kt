package org.odk.collect.audiorecorder.recording

import org.odk.collect.audiorecorder.recorder.Recorder
import java.io.File

class FakeRecorder : Recorder {

    val file: File = File.createTempFile("recording", ".mp3")

    private var recording = false
    private var cancelled = false

    fun isRecording(): Boolean {
        return recording
    }

    fun wasCancelled(): Boolean {
        return cancelled
    }

    override fun start() {
        recording = true
        cancelled = false
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
