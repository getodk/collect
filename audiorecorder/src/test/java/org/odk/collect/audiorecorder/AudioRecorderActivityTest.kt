package org.odk.collect.audiorecorder

import android.app.Activity.RESULT_OK
import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.android.synthetic.main.activity_audio_recorder.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.audiorecorder.recording.AudioRecorderActivity
import org.odk.collect.audiorecorder.recording.Recorder
import java.io.File

@RunWith(AndroidJUnit4::class)
class AudioRecorderActivityTest {

    private val fakeRecorder = FakeRecorder()

    @Before
    fun setup() {
        setAudioRecorderDependencyComponent(DaggerAudioRecorderDependencyComponent.builder()
                .application(ApplicationProvider.getApplicationContext())
                .dependencyModule(object : AudioRecorderDependencyModule() {
                    override fun providesRecorder(application: Application): Recorder {
                        return fakeRecorder
                    }
                })
                .build())
    }

    @Test
    fun launching_startsRecording() {
        launchActivity<AudioRecorderActivity>()
        assertThat(fakeRecorder.isRecording(), equalTo(true))
    }

    @Test
    fun pressingDone_stopsRecording_setsResultToRecordedFile_andFinishes() {
        val scenario = launchActivity<AudioRecorderActivity>().onActivity {
            it.done.performClick()
        }

        assertThat(fakeRecorder.isRecording(), equalTo(false))
        assertThat(scenario.result.resultCode, equalTo(RESULT_OK))
        assertThat(scenario.result.resultData.data, equalTo(Uri.parse(fakeRecorder.file.absolutePath)))
    }
}

private class FakeRecorder : Recorder {

    val file: File = File.createTempFile("recording", ".mp3")

    private var recording = false

    fun isRecording(): Boolean {
        return recording
    }

    override fun start() {
        recording = true
    }

    override fun stop(): File {
        recording = false
        return file
    }
}