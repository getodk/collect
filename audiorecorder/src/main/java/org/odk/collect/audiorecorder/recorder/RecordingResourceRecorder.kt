package org.odk.collect.audiorecorder.recorder

import java.io.File

internal class RecordingResourceRecorder(private val cacheDir: File, private val recordingResourceFactory: (Output) -> RecordingResource) : Recorder {

    override val amplitude: Int
        get() = recordingResource?.getMaxAmplitude() ?: 0

    private var recordingResource: RecordingResource? = null
    private var file: File? = null

    override fun start(output: Output) {
        recordingResource = recordingResourceFactory(output).also {
            val tempFile = when (output) {
                Output.AMR -> {
                    File.createTempFile("recording", ".amr", cacheDir)
                }

                Output.AAC -> {
                    File.createTempFile("recording", ".m4a", cacheDir)
                }
            }

            it.setOutputFile(tempFile.absolutePath)
            file = tempFile

            it.prepare()
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
