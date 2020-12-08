package org.odk.collect.audiorecorder.recorder

import java.io.File
import java.lang.Exception

internal interface Recorder {

    @Throws(RecordingException::class)
    fun start(output: Output)
    fun pause()
    fun resume()
    fun stop(): File
    fun cancel()

    val amplitude: Int
    fun isRecording(): Boolean
}

enum class Output {
    AMR,
    AAC,
    AAC_LOW
}

internal class RecordingException : Exception()
