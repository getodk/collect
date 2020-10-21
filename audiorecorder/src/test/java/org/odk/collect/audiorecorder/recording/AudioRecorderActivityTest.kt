package org.odk.collect.audiorecorder.recording

import android.app.Activity.RESULT_OK
import android.app.Application
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.android.synthetic.main.activity_audio_recorder.done
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.audiorecorder.AudioRecorderDependencyModule
import org.odk.collect.audiorecorder.overrideDependencies
import org.odk.collect.audiorecorder.recorder.Recorder
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
class AudioRecorderActivityTest {

    private val fakeRecorder = FakeRecorder()
    private val recordingRepository = RecordingRepository()

    @Before
    fun setup() {
        getApplicationContext<Application>().overrideDependencies(
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

    @Test
    fun launching_startsRecordingService_withStart() {
        launchActivity<AudioRecorderActivity>().onActivity {
            val nextStartedService = shadowOf(it).nextStartedService
            assertThat(nextStartedService.component?.className, equalTo(AudioRecorderService::class.qualifiedName))
            assertThat(nextStartedService.action, equalTo(AudioRecorderService.ACTION_START))
        }
    }

    @Test
    fun pressingDone_startsRecordingService_withStop() {
        launchActivity<AudioRecorderActivity>().onActivity {
            shadowOf(it).clearStartedServices() // Get rid of start command

            it.done.performClick()

            val nextStartedService = shadowOf(it).nextStartedService
            assertThat(nextStartedService.component?.className, equalTo(AudioRecorderService::class.qualifiedName))
            assertThat(nextStartedService.action, equalTo(AudioRecorderService.ACTION_STOP))
        }
    }

    @Test
    fun whenRecordingAvailable_finishesWithRecordingAsResult() {
        val scenario = launchActivity<AudioRecorderActivity>()

        val recording = File.createTempFile("blah", ".mp3")
        recordingRepository.create(recording)

        assertThat(scenario.result.resultCode, equalTo(RESULT_OK))
        assertThat(scenario.result.resultData.data, equalTo(Uri.parse(recording.absolutePath)))
    }

    @Test
    fun recordingCanHappenMoreThanOnce() {
        val scenario1 = launchActivity<AudioRecorderActivity>()
        val recording1 = File.createTempFile("blah1", ".mp3")
        recordingRepository.create(recording1)

        assertThat(scenario1.result.resultCode, equalTo(RESULT_OK))
        assertThat(scenario1.result.resultData.data, equalTo(Uri.parse(recording1.absolutePath)))

        scenario1.moveToState(Lifecycle.State.DESTROYED)

        val scenario2 = launchActivity<AudioRecorderActivity>()
        val recording2 = File.createTempFile("blah2", ".mp3")
        recordingRepository.create(recording2)

        assertThat(scenario2.result.resultCode, equalTo(RESULT_OK))
        assertThat(scenario2.result.resultData.data, equalTo(Uri.parse(recording2.absolutePath)))
    }
}
