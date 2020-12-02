package org.odk.collect.audiorecorder.recorder

import java.io.File
import java.io.IOException

internal class RecordingResourceRecorder(private val cacheDir: File, private val recordingResourceFactory: (Output) -> RecordingResource) : Recorder {

    override val amplitude: Int
        get() = recordingResource?.getMaxAmplitude() ?: 0

    private var recordingResource: RecordingResource? = null
    private var file: File? = null

    override fun start(output: Output) {
        recordingResource = recordingResourceFactory(output).also {
            val suffix = when (output) {
                Output.AMR -> ".amr"
                Output.AAC -> ".m4a"
            }

            val tempFile = try {
                File.createTempFile("recording", suffix, cacheDir)
            } catch (e: IOException) {
                throw RecordingException()
            }

            it.setOutputFile(tempFile.absolutePath)
            file = tempFile

            try {
                it.prepare()
            } catch (e: IOException) {
                throw RecordingException()
            }

            it.start()
        }
    }

    override fun pause() {
        recordingResource?.pause()
    }

    override fun resume() {
        recordingResource?.resume()
    }

    override fun stop(): File {
        stopAndReleaseMediaRecorder()
        return file!!
    }

    override fun cancel() {
        stopAndReleaseMediaRecorder()
        file?.delete()
    }

    override fun isRecording(): Boolean {
        return recordingResource != null
    }

    private fun stopAndReleaseMediaRecorder() {
        recordingResource?.apply {
            stop()
            release()
        }

        recordingResource = null
    }
}
