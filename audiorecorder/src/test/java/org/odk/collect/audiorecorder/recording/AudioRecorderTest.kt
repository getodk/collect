package org.odk.collect.audiorecorder.recording

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.audiorecorder.recorder.Output
import java.io.File
import java.io.Serializable

abstract class AudioRecorderTest {

    abstract val viewModel: AudioRecorder
    abstract fun runBackground()
    abstract fun getLastRecordedFile(): File?

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
        val recording = viewModel.getCurrentSession()

        runBackground()
        assertThat(recording.getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun getCurrentSession_whenRecording_returnsSessionWithId() {
        val recording = viewModel.getCurrentSession()
        viewModel.start("session1", Output.AAC)

        runBackground()
        assertThat(recording.getOrAwaitValue(), equalTo(RecordingSession("session1", null, 0, 0, false)))
    }

    @Test
    fun getCurrentSession_afterStop_hasRecordedFile() {
        val recording = viewModel.getCurrentSession()
        viewModel.start("session1", Output.AAC)
        viewModel.stop()

        runBackground()
        assertThat(recording.getOrAwaitValue(), equalTo(RecordingSession("session1", getLastRecordedFile(), 0, 0, false)))
    }

    @Test
    fun getCurrentSession_afterCleanUp_isNull() {
        val recording = viewModel.getCurrentSession()
        viewModel.start("session1", Output.AAC)
        viewModel.cleanUp()

        runBackground()
        assertThat(recording.getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun getCurrentSession_whenRecording_isNotPaused() {
        val session = viewModel.getCurrentSession()
        viewModel.start("session", Output.AAC)

        runBackground()
        assertThat(session.getOrAwaitValue()?.paused, equalTo(false))
    }

    @Test
    fun getCurrentSession_afterStop_isNotPaused() {
        val session = viewModel.getCurrentSession()

        viewModel.start("session", Output.AAC)
        viewModel.stop()

        runBackground()
        assertThat(session.getOrAwaitValue()?.paused, equalTo(false))
    }

    @Test
    fun getCurrentSession_afterPause_isPaused() {
        val session = viewModel.getCurrentSession()

        viewModel.start("session", Output.AAC)
        viewModel.pause()

        runBackground()
        assertThat(session.getOrAwaitValue()?.paused, equalTo(true))
    }

    @Test
    fun getCurrentSession_afterPauseAndResume_isNotPaused() {
        val session = viewModel.getCurrentSession()

        viewModel.start("session", Output.AAC)
        viewModel.pause()
        viewModel.resume()

        runBackground()
        assertThat(session.getOrAwaitValue()?.paused, equalTo(false))
    }

    @Test
    fun getCurrentSession_afterPauseAndStop_isNotPaused() {
        val session = viewModel.getCurrentSession()

        viewModel.start("session", Output.AAC)
        viewModel.pause()
        viewModel.stop()

        runBackground()
        assertThat(session.getOrAwaitValue()?.paused, equalTo(false))
    }

    @Test
    fun supportsSerializableSessionIds() {
        val sessionId = SerializableId()
        val session = viewModel.getCurrentSession()
        viewModel.start(sessionId, Output.AAC)

        runBackground()
        assertThat(session.getOrAwaitValue()!!.id, equalTo(sessionId))
    }

    @Test
    fun start_whenAlreadyRecording_doesNothing() {
        val session = viewModel.getCurrentSession()
        viewModel.start("session1", Output.AAC)
        viewModel.start("session2", Output.AAC)

        runBackground()
        assertThat(session.getOrAwaitValue()?.id, equalTo("session1"))
    }
}

private class SerializableId : Serializable
