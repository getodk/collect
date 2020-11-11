package org.odk.collect.audiorecorder.recording.internal

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.odk.collect.audiorecorder.AudioRecorderDependencyModule
import org.odk.collect.audiorecorder.TestApplication
import org.odk.collect.audiorecorder.recorder.Recorder
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModelFactory
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModelTest
import org.odk.collect.audiorecorder.setupDependencies
import org.odk.collect.audiorecorder.support.FakeRecorder
import org.odk.collect.testshared.LiveDataTester
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
class RealAudioRecorderViewModelTest : AudioRecorderViewModelTest() {

    @get:Rule
    val instantTaskExecutor = InstantTaskExecutorRule()
    private val liveDataTester = LiveDataTester()
    private val application by lazy { getApplicationContext<TestApplication>() }

    private val recordingRepository = RecordingRepository()
    private val fakeRecorder = FakeRecorder()

    override val viewModel: AudioRecorderViewModel by lazy {
        AudioRecorderViewModelFactory(application).create(AudioRecorderViewModel::class.java)
    }

    override fun runBackground() {
        while (shadowOf(application).peekNextStartedService() != null) {
            val serviceIntent = shadowOf(application).nextStartedService
            assertThat(serviceIntent.component?.className, equalTo(AudioRecorderService::class.qualifiedName))
            Robolectric.buildService(AudioRecorderService::class.java, serviceIntent)
                .create()
                .startCommand(0, 0)
        }
    }

    override fun getLastRecordedFile(): File? {
        return fakeRecorder.file
    }

    @Before
    fun setup() {
        application.setupDependencies(
            object : AudioRecorderDependencyModule() {
                override fun providesRecorder(application: Application): Recorder {
                    return fakeRecorder
                }

                override fun providesRecordingRepository(): RecordingRepository {
                    return recordingRepository
                }
            }
        )
    }

    @After
    fun teardown() {
        liveDataTester.teardown()
    }
}
