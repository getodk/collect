package org.odk.collect.audiorecorder.recording

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_CANCEL
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_START
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_STOP
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.EXTRA_SESSION_ID
import org.odk.collect.audiorecorder.recording.internal.RealAudioRecorderViewModel
import org.odk.collect.audiorecorder.recording.internal.RecordingSession
import org.odk.collect.testshared.LiveDataTester
import org.robolectric.Shadows.shadowOf
import java.io.File.createTempFile

@RunWith(AndroidJUnit4::class)
class RealAudioRecorderViewModelTest {

    @get:Rule
    val instantTaskExecutor = InstantTaskExecutorRule()
    private val liveDataTester = LiveDataTester()

    private val recordingSession = RecordingSession()

    private val application by lazy { getApplicationContext<Application>() }
    private val viewModel: RealAudioRecorderViewModel by lazy {
        RealAudioRecorderViewModel(application, recordingSession)
    }

    @After
    fun teardown() {
        liveDataTester.teardown()
    }

    @Test
    fun getRecording_returnsSessionRecording() {
        recordingSession.start("123")

        val recording = liveDataTester.activate(viewModel.getRecording("123"))
        assertThat(recording.value, equalTo(null))

        val file = createTempFile("blah", "mp3")
        recordingSession.recordingReady(file)
        assertThat(recording.value!!.absolutePath, equalTo(file.absolutePath))
    }

    @Test
    fun getRecording_doesNotReturnOtherSessionRecordings() {
        recordingSession.start("123")

        val recording = liveDataTester.activate(viewModel.getRecording("456"))
        assertThat(recording.value, equalTo(null))

        val file = createTempFile("blah", "mp3")
        recordingSession.recordingReady(file)
        assertThat(recording.value, equalTo(null))
    }

    @Test
    fun isRecording_whenNoSession_isFalse() {
        val recording = liveDataTester.activate(viewModel.isRecording())
        assertThat(recording.value, equalTo(false))
    }

    @Test
    fun isRecording_whenSessionInProgress_isTrue() {
        recordingSession.start("123")

        val recording = liveDataTester.activate(viewModel.isRecording())
        assertThat(recording.value, equalTo(true))
    }

    @Test
    fun start_startsRecordingService_withStartAction() {
        viewModel.start("mySession")
        val nextStartedService = shadowOf(application).nextStartedService
        assertThat(nextStartedService.component?.className, equalTo(AudioRecorderService::class.qualifiedName))
        assertThat(nextStartedService.action, equalTo(ACTION_START))
        assertThat(nextStartedService.getStringExtra(EXTRA_SESSION_ID), equalTo("mySession"))
    }

    @Test
    fun stop_startsRecordingService_withStopAction() {
        viewModel.stop()
        val nextStartedService = shadowOf(application).nextStartedService
        assertThat(nextStartedService.component?.className, equalTo(AudioRecorderService::class.qualifiedName))
        assertThat(nextStartedService.action, equalTo(ACTION_STOP))
    }

    @Test
    fun cancel_startsRecordingService_withCancelAction() {
        viewModel.cancel()
        val nextStartedService = shadowOf(application).nextStartedService
        assertThat(nextStartedService.component?.className, equalTo(AudioRecorderService::class.qualifiedName))
        assertThat(nextStartedService.action, equalTo(ACTION_CANCEL))
    }

    @Test
    fun endSession_clearsSessionRecording() {
        recordingSession.recordingReady(createTempFile("blah", "mp3"))

        viewModel.endSession()
        assertThat(recordingSession.get().value, equalTo(null))
    }
}
