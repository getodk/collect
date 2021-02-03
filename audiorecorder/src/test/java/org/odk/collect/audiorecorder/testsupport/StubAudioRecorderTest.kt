package org.odk.collect.audiorecorder.testsupport

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.odk.collect.audiorecorder.recording.AudioRecorder
import org.odk.collect.audiorecorder.recording.AudioRecorderTest
import java.io.File

@RunWith(AndroidJUnit4::class)
class StubAudioRecorderTest : AudioRecorderTest() {

    private val stubViewAudioRecorderViewModel: StubAudioRecorder by lazy {
        val tempFile = File.createTempFile("blah", ".whatever")
        StubAudioRecorder(tempFile.absolutePath)
    }

    override val viewModel: AudioRecorder by lazy {
        stubViewAudioRecorderViewModel
    }

    override fun runBackground() {
        // No op
    }

    override fun getLastRecordedFile(): File? {
        return stubViewAudioRecorderViewModel.lastRecording
    }
}
