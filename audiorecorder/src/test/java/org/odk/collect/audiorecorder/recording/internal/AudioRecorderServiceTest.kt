package org.odk.collect.audiorecorder.recording.internal

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.audiorecorder.AudioRecorderDependencyModule
import org.odk.collect.audiorecorder.R
import org.odk.collect.audiorecorder.TestApplication
import org.odk.collect.audiorecorder.recorder.Recorder
import org.odk.collect.audiorecorder.setupDependencies
import org.odk.collect.audiorecorder.support.FakeRecorder
import org.robolectric.Robolectric.buildService
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ServiceController

@RunWith(RobolectricTestRunner::class)
class AudioRecorderServiceTest {

    private val application: TestApplication by lazy { ApplicationProvider.getApplicationContext() }
    private val recordingSession = RecordingSession()
    private val recorder = FakeRecorder()

    @Before
    fun setup() {
        application.setupDependencies(
            object : AudioRecorderDependencyModule() {
                override fun providesRecorder(application: Application): Recorder {
                    return recorder
                }

                override fun providesRecordingSession(): RecordingSession {
                    return recordingSession
                }
            }
        )
    }

    @Test
    fun startAction_starsSessionWithId() {
        val intent = Intent(application, AudioRecorderService::class.java)
        intent.action = AudioRecorderService.ACTION_START
        intent.putExtra(AudioRecorderService.EXTRA_SESSION_ID, "123")

        startService(intent)

        assertThat(recordingSession.get().value, equalTo(Pair("123", null)))
    }

    @Test
    fun startAction_startsInForegroundWithNotification() {
        val intent = Intent(application, AudioRecorderService::class.java)
        intent.action = AudioRecorderService.ACTION_START
        intent.putExtra(AudioRecorderService.EXTRA_SESSION_ID, "123")

        val service = buildService(AudioRecorderService::class.java, intent)
            .create()
            .startCommand(0, 0)

        val notification = shadowOf(service.get()).lastForegroundNotification
        assertThat(notification, not(nullValue()))
        assertThat(shadowOf(notification).contentTitle, equalTo(application.getString(R.string.recording)))
    }

    @Test
    fun startAction_startsRecorder() {
        val intent = Intent(application, AudioRecorderService::class.java)
        intent.action = AudioRecorderService.ACTION_START
        intent.putExtra(AudioRecorderService.EXTRA_SESSION_ID, "123")

        startService(intent)

        assertThat(recorder.isRecording(), equalTo(true))
    }

    @Test
    fun startAction_whenRecordingInProgress_doesNothing() {
        val intent = Intent(application, AudioRecorderService::class.java)
        intent.action = AudioRecorderService.ACTION_START
        intent.putExtra(AudioRecorderService.EXTRA_SESSION_ID, "123")
        startService(intent)

        assertThat(recorder.isRecording(), equalTo(true))

        startService(intent)

        assertThat(recorder.isRecording(), equalTo(true))
        assertThat(recorder.recordings.size, equalTo(1))
    }

    @Test
    fun stopAction_stopsRecorder_stopsSelf_andSetsRecordingOnSession() {
        val startIntent = Intent(application, AudioRecorderService::class.java)
        startIntent.action = AudioRecorderService.ACTION_START
        startIntent.putExtra(AudioRecorderService.EXTRA_SESSION_ID, "123")
        startService(startIntent)

        val stopIntent = Intent(application, AudioRecorderService::class.java)
        stopIntent.action = AudioRecorderService.ACTION_STOP
        val service = startService(stopIntent)

        assertThat(recorder.isRecording(), equalTo(false))
        assertThat(recordingSession.get().value, equalTo(Pair("123", recorder.file)))
        assertThat(shadowOf(service.get()).isStoppedBySelf, equalTo(true))
    }

    @Test
    fun cancelAction_cancelsRecorder_stopsSelf_clearsSession() {
        val startIntent = Intent(application, AudioRecorderService::class.java)
        startIntent.action = AudioRecorderService.ACTION_START
        startIntent.putExtra(AudioRecorderService.EXTRA_SESSION_ID, "123")
        startService(startIntent)

        val cancelIntent = Intent(application, AudioRecorderService::class.java)
        cancelIntent.action = AudioRecorderService.ACTION_CLEAN_UP
        val service = startService(cancelIntent)

        assertThat(recorder.isRecording(), equalTo(false))
        assertThat(recorder.wasCancelled(), equalTo(true))
        assertThat(recordingSession.get().value, equalTo(null))
        assertThat(shadowOf(service.get()).isStoppedBySelf, equalTo(true))
    }

    @Test
    fun whenUserKillsAppFromTaskManager_cancelsRecorder_stopsSelf() {
        val intent = Intent(application, AudioRecorderService::class.java)
        intent.action = AudioRecorderService.ACTION_START
        intent.putExtra(AudioRecorderService.EXTRA_SESSION_ID, "123")

        val service = startService(intent)

        service.get().onTaskRemoved(Intent())

        assertThat(recorder.isRecording(), equalTo(false))
        assertThat(recorder.wasCancelled(), equalTo(true))
        assertThat(shadowOf(service.get()).isStoppedBySelf, equalTo(true))
    }

    private fun startService(intent: Intent): ServiceController<AudioRecorderService> {
        return buildService(AudioRecorderService::class.java, intent)
            .create()
            .startCommand(0, 0)
    }
}
