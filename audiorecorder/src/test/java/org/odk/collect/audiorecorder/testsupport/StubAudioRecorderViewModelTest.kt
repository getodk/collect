package org.odk.collect.audiorecorder.testsupport

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModelTest
import java.io.File

@RunWith(AndroidJUnit4::class)
class StubAudioRecorderViewModelTest : AudioRecorderViewModelTest() {

    private val stubViewAudioRecorderViewModel: StubAudioRecorderViewModel by lazy {
        val tempFile = File.createTempFile("blah", ".whatever")
        StubAudioRecorderViewModel(tempFile.absolutePath)
    }

    override val viewModel: AudioRecorderViewModel by lazy {
        stubViewAudioRecorderViewModel
    }

    override fun runBackground() {
        // No op
    }

    override fun getLastRecordedFile(): File? {
        return stubViewAudioRecorderViewModel.lastRecording
    }
}
