package org.odk.collect.audiorecorder.recording

import java.io.File

internal interface Recorder {
    fun start()
    fun stop(): File
    fun cancel()
}
