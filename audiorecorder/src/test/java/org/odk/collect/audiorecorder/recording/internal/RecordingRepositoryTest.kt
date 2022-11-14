package org.odk.collect.audiorecorder.recording.internal

import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.audiorecorder.recording.RecordingSession
import org.odk.collect.audiorecorder.testsupport.RobolectricApplication
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class RecordingRepositoryTest {

    private val application: RobolectricApplication by lazy { ApplicationProvider.getApplicationContext() }

    @Test
    fun `creating new recordingRepository takes into account currentSession and failedToStart from app state`() {
        val session = RecordingSession("Blah 1", null, 0, 0, false)
        val exception = Consumable(Exception())

        application.getState().set("currentSession", MutableLiveData(session))
        application.getState().set("failedToStart", MutableLiveData(exception))

        val recordingRepository = RecordingRepository(application.getState())

        assertThat(recordingRepository.currentSession.getOrAwaitValue(), `is`(session))
        assertThat(recordingRepository.failedToStart.getOrAwaitValue(), `is`(exception))
    }

    @Test
    fun `start() creates new session`() {
        val oldSession = RecordingSession("Blah 1", null, 0, 0, false)
        val newSession = RecordingSession("Blah 2", null, 0, 0, false)

        application.getState().set("currentSession", MutableLiveData(oldSession))
        val recordingRepository = RecordingRepository(application.getState())

        recordingRepository.start("Blah 2")

        assertThat(recordingRepository.currentSession.getOrAwaitValue(), `is`(newSession))
    }

    @Test
    fun `start() clears failedToStart`() {
        val exception = Consumable(Exception())

        application.getState().set("failedToStart", MutableLiveData(exception))
        val recordingRepository = RecordingRepository(application.getState())

        recordingRepository.start("Blah")

        assertThat(recordingRepository.failedToStart.getOrAwaitValue(), equalTo(Consumable(null)))
    }

    @Test
    fun `restart() clears file in current session`() {
        val session = RecordingSession("Blah", File("/blah"), 0, 0, false)

        application.getState().set("currentSession", MutableLiveData(session))
        val recordingRepository = RecordingRepository(application.getState())

        recordingRepository.restart()

        assertThat(recordingRepository.currentSession.getOrAwaitValue(), `is`(session.copy(file = null)))
    }

    @Test
    fun `restart() clears failedToStart`() {
        val exception = Consumable(Exception())

        application.getState().set("failedToStart", MutableLiveData(exception))
        val recordingRepository = RecordingRepository(application.getState())

        recordingRepository.restart()

        assertThat(recordingRepository.failedToStart.getOrAwaitValue(), equalTo(Consumable(null)))
    }

    @Test
    fun `setDuration() updates duration in current session`() {
        val session = RecordingSession("Blah", null, 0, 0, false)

        application.getState().set("currentSession", MutableLiveData(session))
        val recordingRepository = RecordingRepository(application.getState())

        recordingRepository.setDuration(50)

        assertThat(recordingRepository.currentSession.getOrAwaitValue(), equalTo(session.copy(duration = 50)))
    }

    @Test
    fun `setAmplitude() updates amplitude in current session`() {
        val session = RecordingSession("Blah", null, 0, 0, false)

        application.getState().set("currentSession", MutableLiveData(session))
        val recordingRepository = RecordingRepository(application.getState())

        recordingRepository.setAmplitude(50)

        assertThat(recordingRepository.currentSession.getOrAwaitValue(), equalTo(session.copy(amplitude = 50)))
    }

    @Test
    fun `setPaused() updates paused in current session`() {
        val session = RecordingSession("Blah", null, 0, 0, false)

        application.getState().set("currentSession", MutableLiveData(session))
        val recordingRepository = RecordingRepository(application.getState())

        recordingRepository.setPaused(true)

        assertThat(recordingRepository.currentSession.getOrAwaitValue(), equalTo(session.copy(paused = true)))
    }

    @Test
    fun `recordingReady() updates file and paused in current session`() {
        val session = RecordingSession("Blah", null, 0, 0, true)

        application.getState().set("currentSession", MutableLiveData(session))
        val recordingRepository = RecordingRepository(application.getState())

        val file = File("/blah")
        recordingRepository.recordingReady(file)

        assertThat(recordingRepository.currentSession.getOrAwaitValue(), equalTo(session.copy(file = file, paused = false)))
    }

    @Test
    fun `clear() clears current session`() {
        val session = RecordingSession("Blah", null, 0, 0, false)

        application.getState().set("currentSession", MutableLiveData(session))
        val recordingRepository = RecordingRepository(application.getState())

        recordingRepository.clear()

        assertThat(recordingRepository.currentSession.getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun `failToStart() clears current session`() {
        val session = RecordingSession("Blah", null, 0, 0, false)

        application.getState().set("currentSession", MutableLiveData(session))
        val recordingRepository = RecordingRepository(application.getState())

        recordingRepository.failToStart(Exception())

        assertThat(recordingRepository.currentSession.getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun `failToStart() updates failedToStart`() {
        val exception = Exception()
        val recordingRepository = RecordingRepository(application.getState())

        assertThat(recordingRepository.failedToStart.getOrAwaitValue(), equalTo(Consumable(null)))

        recordingRepository.failToStart(exception)

        assertThat(recordingRepository.failedToStart.getOrAwaitValue().value, equalTo(exception))
    }
}
