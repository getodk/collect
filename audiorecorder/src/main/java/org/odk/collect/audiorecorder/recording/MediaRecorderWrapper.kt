package org.odk.collect.audiorecorder.recording

import android.media.MediaRecorder

/**
 * Allows faking/stubbing/mocking with our interactions with Android's MediaRecorder
 */

interface MediaRecorderWrapper {
    fun setAudioSource(audioSource: Int)
    fun setOutputFormat(outputFormat: Int)
    fun setOutputFile(path: String)
    fun setAudioEncoder(audioEncoder: Int)
    fun prepare()
    fun start()
    fun stop()
    fun release()
}

class RealMediaRecorderWrapper(private val mediaRecorder: MediaRecorder) : MediaRecorderWrapper {

    override fun setAudioSource(audioSource: Int) {
        mediaRecorder.setAudioSource(audioSource)
    }

    override fun setOutputFormat(outputFormat: Int) {
        mediaRecorder.setOutputFormat(outputFormat)
    }

    override fun setOutputFile(path: String) {
        mediaRecorder.setOutputFile(path)
    }

    override fun setAudioEncoder(audioEncoder: Int) {
        mediaRecorder.setAudioEncoder(audioEncoder)
    }

    override fun prepare() {
        mediaRecorder.prepare()
    }

    override fun start() {
        mediaRecorder.start()
    }

    override fun stop() {
        mediaRecorder.stop()
    }

    override fun release() {
        mediaRecorder.release()
    }

}
