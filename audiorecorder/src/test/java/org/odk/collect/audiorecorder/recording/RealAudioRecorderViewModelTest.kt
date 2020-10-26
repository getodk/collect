package org.odk.collect.audiorecorder.recording

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_CANCEL
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_START
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_STOP
import org.odk.collect.audiorecorder.recording.internal.RealAudioRecorderViewModel
import org.odk.collect.audiorecorder.recording.internal.RecordingSession
import org.robolectric.Shadows.shadowOf
import java.io.File.createTempFile

@RunWith(AndroidJUnit4::class)
class RealAudioRecorderViewModelTest {

    private val recordingSession = RecordingSession()

    private val application by lazy { getApplicationContext<Application>() }
    private val viewModel: RealAudioRecorderViewModel by lazy {
        RealAudioRecorderViewModel(application, recordingSession)
    }

    @Test
    fun recording_returnsSessionRecording() {
        val recording = viewModel.recording
        assertThat(recording.value, equalTo(null))

        val file = createTempFile("blah", "mp3")
        recordingSession.recordingReady(file)
        assertThat(recording.value!!.absolutePath, equalTo(file.absolutePath))
    }

    @Test
    fun start_startsRecordingService_withStartAction() {
        viewModel.start()
        val nextStartedService = shadowOf(application).nextStartedService
        assertThat(nextStartedService.component?.className, equalTo(AudioRecorderService::class.qualifiedName))
        assertThat(nextStartedService.action, equalTo(ACTION_START))
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
        assertThat(recordingSession.getRecording().value, equalTo(null))
    }
}
