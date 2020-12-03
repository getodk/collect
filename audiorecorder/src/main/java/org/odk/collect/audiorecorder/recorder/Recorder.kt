package org.odk.collect.audiorecorder.recorder

import java.io.File

internal interface Recorder {
    fun start(output: Output)
    fun stop(): File
    fun cancel()

    val amplitude: Int
    fun isRecording(): Boolean
}

enum class Output {
    AMR,
    AAC
}
