package org.odk.collect.audiorecorder.recording

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.audiorecorder.recorder.Output
import java.io.File
import java.io.Serializable

abstract class AudioRecorderTest {

    abstract val audioRecorder: AudioRecorder
    abstract fun runBackground()
    abstract fun getLastRecordedFile(): File?

    @Test
    fun isRecording_whenNoSession_isFalse() {
        runBackground()
        assertThat(audioRecorder.isRecording(), equalTo(false))
    }

    @Test
    fun isRecording_whenRecording_isTrue() {
        audioRecorder.start("session1", Output.AAC)

        runBackground()
        assertThat(audioRecorder.isRecording(), equalTo(true))
    }

    @Test
    fun isRecording_afterRestart_isTrue() {
        audioRecorder.start("session1", Output.AAC)
        audioRecorder.restart()

        runBackground()
        assertThat(audioRecorder.isRecording(), equalTo(true))
    }

    @Test
    fun isRecording_afterStop_isFalse() {
        audioRecorder.start("session1", Output.AAC)
        audioRecorder.stop()

        runBackground()
        assertThat(audioRecorder.isRecording(), equalTo(false))
    }

    @Test
    fun isRecording_afterCleanUp_isFalse() {
        audioRecorder.start("session1", Output.AAC)
        audioRecorder.cleanUp()

        runBackground()
        assertThat(audioRecorder.isRecording(), equalTo(false))
    }

    @Test
    fun getCurrentSession_beforeRecording_isNull() {
        val recording = audioRecorder.getCurrentSession()

        runBackground()
        assertThat(recording.getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun getCurrentSession_whenRecording_returnsSessionWithId() {
        val recording = audioRecorder.getCurrentSession()
        audioRecorder.start("session1", Output.AAC)

        runBackground()
        assertThat(recording.getOrAwaitValue(), equalTo(RecordingSession("session1", null, 0, 0, false)))
    }

    @Test
    fun getCurrentSession_afterRestart_returnsSessionWithId() {
        val recording = audioRecorder.getCurrentSession()
        audioRecorder.start("session1", Output.AAC)
        audioRecorder.restart()

        runBackground()
        assertThat(recording.getOrAwaitValue(), equalTo(RecordingSession("session1", null, 0, 0, false)))
    }

    @Test
    fun getCurrentSession_afterStop_hasRecordedFile() {
        val recording = audioRecorder.getCurrentSession()
        audioRecorder.start("session1", Output.AAC)
        audioRecorder.stop()

        runBackground()
        assertThat(recording.getOrAwaitValue(), equalTo(RecordingSession("session1", getLastRecordedFile(), 0, 0, false)))
    }

    @Test
    fun getCurrentSession_afterCleanUp_isNull() {
        val recording = audioRecorder.getCurrentSession()
        audioRecorder.start("session1", Output.AAC)
        audioRecorder.cleanUp()

        runBackground()
        assertThat(recording.getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun getCurrentSession_whenRecording_isNotPaused() {
        val session = audioRecorder.getCurrentSession()
        audioRecorder.start("session", Output.AAC)

        runBackground()
        assertThat(session.getOrAwaitValue()?.paused, equalTo(false))
    }

    @Test
    fun getCurrentSession_afterRestart_isNotPaused() {
        val session = audioRecorder.getCurrentSession()
        audioRecorder.start("session", Output.AAC)
        audioRecorder.restart()

        runBackground()
        assertThat(session.getOrAwaitValue()?.paused, equalTo(false))
    }

    @Test
    fun getCurrentSession_afterStop_isNotPaused() {
        val session = audioRecorder.getCurrentSession()

        audioRecorder.start("session", Output.AAC)
        audioRecorder.stop()

        runBackground()
        assertThat(session.getOrAwaitValue()?.paused, equalTo(false))
    }

    @Test
    fun getCurrentSession_afterPause_isPaused() {
        val session = audioRecorder.getCurrentSession()

        audioRecorder.start("session", Output.AAC)
        audioRecorder.pause()

        runBackground()
        assertThat(session.getOrAwaitValue()?.paused, equalTo(true))
    }

    @Test
    fun getCurrentSession_afterPauseAndResume_isNotPaused() {
        val session = audioRecorder.getCurrentSession()

        audioRecorder.start("session", Output.AAC)
        audioRecorder.pause()
        audioRecorder.resume()

        runBackground()
        assertThat(session.getOrAwaitValue()?.paused, equalTo(false))
    }

    @Test
    fun getCurrentSession_afterPauseAndStop_isNotPaused() {
        val session = audioRecorder.getCurrentSession()

        audioRecorder.start("session", Output.AAC)
        audioRecorder.pause()
        audioRecorder.stop()

        runBackground()
        assertThat(session.getOrAwaitValue()?.paused, equalTo(false))
    }

    @Test
    fun supportsSerializableSessionIds() {
        val sessionId = SerializableId()
        val session = audioRecorder.getCurrentSession()
        audioRecorder.start(sessionId, Output.AAC)

        runBackground()
        assertThat(session.getOrAwaitValue()!!.id, equalTo(sessionId))
    }

    @Test
    fun start_whenAlreadyRecording_doesNothing() {
        val session = audioRecorder.getCurrentSession()
        audioRecorder.start("session1", Output.AAC)
        audioRecorder.start("session2", Output.AAC)

        runBackground()
        assertThat(session.getOrAwaitValue()?.id, equalTo("session1"))
    }
}

private class SerializableId : Serializable
