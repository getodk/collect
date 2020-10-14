package org.odk.collect.audiorecorder.recording

import java.io.File

interface Recorder {
    fun start()
    fun stop(): File
}
