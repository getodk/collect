package org.odk.collect.audiorecorder.recorder

import com.google.common.io.Files
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Test
import org.odk.collect.audiorecorder.recording.MicInUseException
import org.odk.collect.audiorecorder.recording.SetupException
import java.io.File
import java.io.IOException

class RecordingResourceRecorderTest {

    private val cacheDir = Files.createTempDir()
    private val recordingResource = FakeRecordingResource()

    private var lastOutput: Output? = null
    private val recorder = RecordingResourceRecorder(cacheDir) { output ->
        lastOutput = output
        recordingResource
    }

    @Test
    fun start_startsMediaRecorder() {
        recorder.start(Output.AAC)
        assertThat(recordingResource.hasStarted(), equalTo(true))
    }

    @Test
    fun start_usesOutputToCreateResourceRecorder() {
        Output.values().forEach {
            RecordingResourceRecorder(cacheDir) { output ->
                lastOutput = output
                FakeRecordingResource()
            }.start(it)

            assertThat(lastOutput, equalTo(it))
        }
    }

    @Test
    fun start_createsAndRecordsToM4AFileInCacheDir() {
        recorder.start(Output.AAC)
        assertThat(recordingResource.getOutputFile()!!.parent, equalTo(cacheDir.absolutePath))
        assertThat(recordingResource.getOutputFile()!!.absolutePath, endsWith(".m4a"))
    }

    @Test
    fun start_createsAndRecordsToAMRFileInCacheDir() {
        recorder.start(Output.AMR)
        assertThat(recordingResource.getOutputFile()!!.parent, equalTo(cacheDir.absolutePath))
        assertThat(recordingResource.getOutputFile()!!.absolutePath, endsWith(".amr"))
    }

    @Test
    fun start_createsAndRecordsToAACLOWFileInCacheDir() {
        recorder.start(Output.AAC_LOW)
        assertThat(recordingResource.getOutputFile()!!.parent, equalTo(cacheDir.absolutePath))
        assertThat(recordingResource.getOutputFile()!!.absolutePath, endsWith(".m4a"))
    }

    @Test
    fun start_setsIsRecording_toTrue() {
        recorder.start(Output.AAC)
        assertThat(recorder.isRecording(), equalTo(true))
    }

    @Test
    fun recordingTwice_doesntUseSameOutputFile() {
        var mediaRecorder = FakeRecordingResource()
        var recorder = RecordingResourceRecorder(cacheDir) { mediaRecorder }
        recorder.start(Output.AAC)
        val outputFile1 = mediaRecorder.getOutputFile()

        mediaRecorder = FakeRecordingResource()
        recorder = RecordingResourceRecorder(cacheDir) { mediaRecorder }
        recorder.start(Output.AAC)
        val outputFile2 = mediaRecorder.getOutputFile()

        assertThat(outputFile1!!.absolutePath, not(equalTo(outputFile2!!.absolutePath)))
    }

    @Test(expected = SetupException::class)
    fun start_whenFileCantBeCreated_throwsSetupException() {
        cacheDir.deleteRecursively()
        recorder.start(Output.AAC)
    }

    @Test(expected = SetupException::class)
    fun start_whenPrepareFails_throwsSetupException() {
        recordingResource.failOnPrepare()
        recorder.start(Output.AAC)
    }

    @Test(expected = MicInUseException::class)
    fun start_whenMicIsInUse_throwsMicInUseException() {
        recordingResource.micInUse()
        recorder.start(Output.AAC)
    }

    @Test
    fun stop_releasesMediaRecorder() {
        recorder.start(Output.AAC)
        recorder.stop()
        assertThat(recordingResource.isReleased(), equalTo(true))
    }

    @Test
    fun stop_returnsOutputFile() {
        recorder.start(Output.AAC)
        val file = recorder.stop()
        assertThat(file.absolutePath, equalTo(recordingResource.getOutputFile()!!.absolutePath))
    }

    @Test
    fun stop_setsIsRecording_toFalse() {
        recorder.start(Output.AAC)
        recorder.stop()
        assertThat(recorder.isRecording(), equalTo(false))
    }

    @Test
    fun cancel_releasesMediaRecorder() {
        recorder.start(Output.AAC)
        recorder.cancel()
        assertThat(recordingResource.isReleased(), equalTo(true))
    }

    @Test
    fun cancel_deletesOutputFile() {
        recorder.start(Output.AAC)
        recorder.cancel()
        assertThat(recordingResource.getOutputFile()!!.exists(), equalTo(false))
    }

    @Test
    fun cancel_setsIsRecording_toFalse() {
        recorder.start(Output.AAC)
        recorder.cancel()
        assertThat(recorder.isRecording(), equalTo(false))
    }

    @Test
    fun cancel_beforeStart_works() {
        recorder.cancel()
    }

    @Test
    fun pause_pausesMediaRecorder() {
        recorder.start(Output.AAC)
        recorder.pause()
        assertThat(recordingResource.isPaused(), equalTo(true))
    }

    @Test
    fun resume_resumesMediaRecorder() {
        recorder.start(Output.AAC)
        recorder.pause()
        recorder.resume()
        assertThat(recordingResource.isPaused(), equalTo(false))
    }
}

private class FakeRecordingResource : RecordingResource {

    private var file: File? = null

    private var started = false
    private var prepared = false
    private var released = false
    private var paused = false
    private var failOnPrepare = false
    private var micInUse = false

    override fun setOutputFile(path: String) {
        if (prepared) {
            throw IllegalStateException("MediaRecorder already prepared!")
        }

        if (!File(path).exists()) {
            throw IllegalArgumentException("Path doesn't refer to created file!")
        }

        file = File(path)
    }

    @Throws(IOException::class)
    override fun prepare() {
        if (failOnPrepare) {
            throw IOException()
        } else {
            prepared = true
        }
    }

    override fun start() {
        if (!prepared) {
            throw IllegalStateException("MediaRecorder not prepared!")
        }

        if (micInUse) {
            throw IllegalStateException()
        }

        started = true
    }

    override fun pause() {
        if (!started) {
            throw IllegalStateException("MediaRecorder not started!")
        }

        paused = true
    }

    override fun resume() {
        if (!started) {
            throw IllegalStateException("MediaRecorder not started!")
        }

        paused = false
    }

    override fun stop() {
        started = false
    }

    override fun release() {
        if (started) {
            throw IllegalStateException("Cannot release while recording! Call stop() first")
        }

        released = true
    }

    override fun getMaxAmplitude(): Int {
        return 0
    }

    fun hasStarted(): Boolean {
        return started
    }

    fun getOutputFile(): File? {
        return file
    }

    fun isReleased(): Boolean {
        return released
    }

    fun isPaused(): Boolean {
        return paused
    }

    fun failOnPrepare() {
        failOnPrepare = true
    }

    fun micInUse() {
        micInUse = true
    }
}
