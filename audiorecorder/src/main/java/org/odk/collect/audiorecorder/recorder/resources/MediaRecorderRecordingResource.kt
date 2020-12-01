package org.odk.collect.audiorecorder.recorder.resources

import android.media.MediaRecorder
import org.odk.collect.audiorecorder.recorder.RecordingResource

internal class MediaRecorderRecordingResource(private val mediaRecorder: MediaRecorder) : RecordingResource {

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

    override fun setAudioEncodingSampleRate(sampleRate: Int) {
        mediaRecorder.setAudioSamplingRate(sampleRate)
    }

    override fun setAudioEncodingBitRate(bitRate: Int) {
        mediaRecorder.setAudioEncodingBitRate(bitRate)
    }

    override fun prepare() {
        mediaRecorder.prepare()
    }

    override fun start() {
        mediaRecorder.start()
    }

    override fun pause() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mediaRecorder.pause()
        }
    }

    override fun resume() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mediaRecorder.resume()
        }
    }

    override fun stop() {
        mediaRecorder.stop()
    }

    override fun release() {
        mediaRecorder.release()
    }

    override fun getMaxAmplitude(): Int {
        return mediaRecorder.maxAmplitude
    }
}
