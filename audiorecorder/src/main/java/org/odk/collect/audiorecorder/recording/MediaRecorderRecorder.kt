package org.odk.collect.audiorecorder.recording

import android.media.MediaRecorder
import java.io.File


internal class MediaRecorderRecorder(private val cacheDir: File) : Recorder {

    private var mediaRecorder: MediaRecorder? = null
    private var file: File? = null

    override fun start() {
        val tempFile: File = File.createTempFile("recording", ".m4a", cacheDir)
        file = tempFile

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(tempFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
            start()
        }
    }

    override fun stop(): File {
        mediaRecorder?.apply {
            stop()
            release()
        }

        return file!!
    }

    override fun cancel() {
        stop()
    }
}