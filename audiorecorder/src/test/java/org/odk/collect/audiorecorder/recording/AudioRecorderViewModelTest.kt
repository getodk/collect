package org.odk.collect.audiorecorder.recording

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Test
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.testshared.LiveDataTester
import java.io.File

abstract class AudioRecorderViewModelTest {

    private val liveDataTester = LiveDataTester()

    abstract val viewModel: AudioRecorderViewModel
    abstract fun runBackground()
    abstract fun getLastRecordedFile(): File?

    @After
    fun teardown() {
        liveDataTester.teardown()
    }

    @Test
    fun isRecording_whenNoSession_isFalse() {
        val recording = liveDataTester.activate(viewModel.isRecording())

        runBackground()
        assertThat(recording.value, equalTo(false))
    }

    @Test
    fun isRecording_whenRecording_isTrue() {
        val recording = liveDataTester.activate(viewModel.isRecording())
        viewModel.start("session1", Output.AAC)

        runBackground()
        assertThat(recording.value, equalTo(true))
    }

    @Test
    fun isRecording_afterStop_isFalse() {
        val recording = liveDataTester.activate(viewModel.isRecording())
        viewModel.start("session1", Output.AAC)
        viewModel.stop()

        runBackground()
        assertThat(recording.value, equalTo(false))
    }

    @Test
    fun isRecording_afterCleanUp_isFalse() {
        val recording = liveDataTester.activate(viewModel.isRecording())
        viewModel.start("session1", Output.AAC)
        viewModel.cleanUp()

        runBackground()
        assertThat(recording.value, equalTo(false))
    }

    @Test
    fun getRecording_beforeRecording_isNull() {
        val recording = liveDataTester.activate(viewModel.getRecording("session1"))

        runBackground()
        assertThat(recording.value, equalTo(null))
    }

    @Test
    fun getRecording_whenRecording_isNull() {
        val recording = liveDataTester.activate(viewModel.getRecording("session1"))
        viewModel.start("session1", Output.AAC)

        runBackground()
        assertThat(recording.value, equalTo(null))
    }

    @Test
    fun getRecording_afterStop_isRecordedFile() {
        val recording = liveDataTester.activate(viewModel.getRecording("session1"))
        viewModel.start("session1", Output.AAC)
        viewModel.stop()

        runBackground()
        assertThat(recording.value, equalTo(getLastRecordedFile()))
    }

    @Test
    fun getRecording_afterCleanUp_isNull() {
        val recording = liveDataTester.activate(viewModel.getRecording("session1"))
        viewModel.start("session1", Output.AAC)
        viewModel.cleanUp()

        runBackground()
        assertThat(recording.value, equalTo(null))
    }

    @Test
    fun getRecording_worksForMultipleSessions() {
        val recording1 = liveDataTester.activate(viewModel.getRecording("session1"))
        val recording2 = liveDataTester.activate(viewModel.getRecording("session2"))
        viewModel.start("session2", Output.AAC)
        viewModel.stop()

        runBackground()
        val recording2File = getLastRecordedFile()
        assertThat(recording1.value, equalTo(null))
        assertThat(recording2.value, equalTo(recording2File))

        viewModel.start("session1", Output.AAC)
        viewModel.stop()

        runBackground()
        val recording1File = getLastRecordedFile()
        assertThat(recording1.value, equalTo(recording1File))
        assertThat(recording2.value, equalTo(recording2File))

        // Check cleanup cleans up everything
        viewModel.cleanUp()
        runBackground()
        assertThat(recording1.value, equalTo(null))
        assertThat(recording2.value, equalTo(null))
    }
}
