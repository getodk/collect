package org.odk.collect.audiorecorder.recording

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.audiorecorder.recording.AudioRecorderService.Companion.ACTION_CANCEL
import org.odk.collect.audiorecorder.recording.AudioRecorderService.Companion.ACTION_START
import org.odk.collect.audiorecorder.recording.AudioRecorderService.Companion.ACTION_STOP
import org.robolectric.Shadows.shadowOf
import java.io.File.createTempFile

@RunWith(AndroidJUnit4::class)
class AudioRecorderViewModelTest {

    private val recordingRepository = RecordingRepository()

    private val application by lazy { getApplicationContext<Application>() }
    private val viewModel: AudioRecorderViewModel by lazy {
        AudioRecorderViewModel(application, recordingRepository)
    }

    @Test
    fun recording_returnsRepositoryRecording() {
        val recording = viewModel.recording
        assertThat(recording.value, equalTo(null))

        val file = createTempFile("blah", "mp3")
        recordingRepository.create(file)
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
    fun endSession_clearsRepository() {
        recordingRepository.create(createTempFile("blah", "mp3"))

        viewModel.endSession()
        assertThat(recordingRepository.getRecording().value, equalTo(null))
    }
}
