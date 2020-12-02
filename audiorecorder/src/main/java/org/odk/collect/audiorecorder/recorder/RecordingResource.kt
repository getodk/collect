package org.odk.collect.audiorecorder.recorder

/**
 * Allows faking/stubbing/mocking with our interactions with Android's MediaRecorder. Could also
 * wrap multiple implementations in the future.
 */

internal interface RecordingResource {
    fun setOutputFile(path: String)
    fun prepare()
    fun start()
    fun pause()
    fun resume()
    fun stop()
    fun release()
    fun getMaxAmplitude(): Int
}
