package org.odk.collect.audiorecorder.recording

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Test
import org.odk.collect.androidtest.LiveDataTester
import org.odk.collect.audiorecorder.recorder.Output
import java.io.File
import java.io.Serializable

abstract class AudioRecorderTest {

    private val liveDataTester = LiveDataTester()

    abstract val viewModel: AudioRecorder
    abstract fun runBackground()
    abstract fun getLastRecordedFile(): File?

    @After
    fun teardown() {
        liveDataTester.teardown()
    }

    @Test
    fun isRecording_whenNoSession_isFalse() {
        runBackground()
        assertThat(viewModel.isRecording(), equalTo(false))
    }

    @Test
    fun isRecording_whenRecording_isTrue() {
        viewModel.start("session1", Output.AAC)

        runBackground()
        assertThat(viewModel.isRecording(), equalTo(true))
    }

    @Test
    fun isRecording_afterStop_isFalse() {
        viewModel.start("session1", Output.AAC)
        viewModel.stop()

        runBackground()
        assertThat(viewModel.isRecording(), equalTo(false))
    }

    @Test
    fun isRecording_afterCleanUp_isFalse() {
        viewModel.start("session1", Output.AAC)
        viewModel.cleanUp()

        runBackground()
        assertThat(viewModel.isRecording(), equalTo(false))
    }

    @Test
    fun getCurrentSession_beforeRecording_isNull() {
        val recording = liveDataTester.activate(viewModel.getCurrentSession())

        runBackground()
        assertThat(recording.value, equalTo(null))
    }

    @Test
    fun getCurrentSession_whenRecording_returnsSessionWithId() {
        val recording = liveDataTester.activate(viewModel.getCurrentSession())
        viewModel.start("session1", Output.AAC)

        runBackground()
        assertThat(recording.value, equalTo(RecordingSession("session1", null, 0, 0, false)))
    }

    @Test
    fun getCurrentSession_afterStop_hasRecordedFile() {
        val recording = liveDataTester.activate(viewModel.getCurrentSession())
        viewModel.start("session1", Output.AAC)
        viewModel.stop()

        runBackground()
        assertThat(recording.value, equalTo(RecordingSession("session1", getLastRecordedFile(), 0, 0, false)))
    }

    @Test
    fun getCurrentSession_afterCleanUp_isNull() {
        val recording = liveDataTester.activate(viewModel.getCurrentSession())
        viewModel.start("session1", Output.AAC)
        viewModel.cleanUp()

        runBackground()
        assertThat(recording.value, equalTo(null))
    }

    @Test
    fun getCurrentSession_whenRecording_isNotPaused() {
        val session = liveDataTester.activate(viewModel.getCurrentSession())
        viewModel.start("session", Output.AAC)

        runBackground()
        assertThat(session.value?.paused, equalTo(false))
    }

    @Test
    fun getCurrentSession_afterStop_isNotPaused() {
        val session = liveDataTester.activate(viewModel.getCurrentSession())

        viewModel.start("session", Output.AAC)
        viewModel.stop()

        runBackground()
        assertThat(session.value?.paused, equalTo(false))
    }

    @Test
    fun getCurrentSession_afterPause_isPaused() {
        val session = liveDataTester.activate(viewModel.getCurrentSession())

        viewModel.start("session", Output.AAC)
        viewModel.pause()

        runBackground()
        assertThat(session.value?.paused, equalTo(true))
    }

    @Test
    fun getCurrentSession_afterPauseAndResume_isNotPaused() {
        val session = liveDataTester.activate(viewModel.getCurrentSession())

        viewModel.start("session", Output.AAC)
        viewModel.pause()
        viewModel.resume()

        runBackground()
        assertThat(session.value?.paused, equalTo(false))
    }

    @Test
    fun getCurrentSession_afterPauseAndStop_isNotPaused() {
        val session = liveDataTester.activate(viewModel.getCurrentSession())

        viewModel.start("session", Output.AAC)
        viewModel.pause()
        viewModel.stop()

        runBackground()
        assertThat(session.value?.paused, equalTo(false))
    }

    @Test
    fun supportsSerializableSessionIds() {
        val sessionId = SerializableId()
        val session = liveDataTester.activate(viewModel.getCurrentSession())
        viewModel.start(sessionId, Output.AAC)

        runBackground()
        assertThat(session.value!!.id, equalTo(sessionId))
    }

    @Test
    fun start_whenAlreadyRecording_doesNothing() {
        val session = liveDataTester.activate(viewModel.getCurrentSession())
        viewModel.start("session1", Output.AAC)
        viewModel.start("session2", Output.AAC)

        runBackground()
        assertThat(session.value?.id, equalTo("session1"))
    }
}

private class SerializableId : Serializable
