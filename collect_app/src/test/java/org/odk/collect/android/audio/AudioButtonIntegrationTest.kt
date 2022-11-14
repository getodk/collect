package org.odk.collect.android.audio

import android.media.MediaPlayer
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.R
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidtest.FakeLifecycleOwner
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.audioclips.Clip
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.RobolectricHelpers.setupMediaPlayerDataSource
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowMediaPlayer
import java.io.File

@RunWith(AndroidJUnit4::class)
class AudioButtonIntegrationTest {
    private val mediaPlayer = MediaPlayer()
    private lateinit var activity: FragmentActivity
    private lateinit var activityController: ActivityController<FragmentActivity>
    private lateinit var audioHelper: AudioHelper
    private lateinit var fakeLifecycleOwner: FakeLifecycleOwner
    private lateinit var fakeScheduler: FakeScheduler

    @Before
    fun setup() {
        activityController = Robolectric.buildActivity(
            FragmentActivity::class.java
        )
        activity = activityController.setup().get()
        activity.setTheme(R.style.Theme_MaterialComponents)
        fakeLifecycleOwner = FakeLifecycleOwner()
        fakeScheduler = FakeScheduler()
        audioHelper = AudioHelper(activity, fakeLifecycleOwner, fakeScheduler) { mediaPlayer }
    }

    @Test
    fun `can play and stop audio`() {
        val testFile = File.createTempFile("audio", ".mp3").absolutePath
        val dataSource = setupMediaPlayerDataSource(testFile)
        val button = AudioButton(activity)
        audioHelper.setAudio(button, Clip("clip1", testFile))

        assertFalse(button.isPlaying)

        button.performClick()

        assertTrue(mediaPlayer.isPlaying)

        assertThat(
            Shadows.shadowOf(mediaPlayer).dataSource,
            equalTo(dataSource)
        )

        assertTrue(button.isPlaying)

        button.performClick()

        assertFalse(mediaPlayer.isPlaying)
        assertFalse(button.isPlaying)
    }

    @Test
    fun `playing audio stops other audio`() {
        val testFile1 = File.createTempFile("audio1", ".mp3").absolutePath
        val testFile2 = File.createTempFile("audio2", ".mp3").absolutePath
        setupMediaPlayerDataSource(testFile1)
        val dataSource2 = setupMediaPlayerDataSource(testFile2)
        val button1 = AudioButton(activity)
        audioHelper.setAudio(button1, Clip("clip1", testFile1))
        val button2 = AudioButton(activity)
        audioHelper.setAudio(button2, Clip("clip2", testFile2))
        button1.performClick()
        button2.performClick()

        assertTrue(mediaPlayer.isPlaying)
        assertThat(
            Shadows.shadowOf(mediaPlayer).dataSource,
            equalTo(dataSource2)
        )
        assertTrue(button2.isPlaying)
        assertFalse(button1.isPlaying)
    }

    @Test
    fun `when two buttons use the same file but different clip IDs and one is played they don't both play`() {
        val testFile1 = File.createTempFile("audio1", ".mp3").absolutePath
        setupMediaPlayerDataSource(testFile1)
        val button1 = AudioButton(activity)
        audioHelper.setAudio(button1, Clip("clip1", testFile1))
        val button2 = AudioButton(activity)
        audioHelper.setAudio(button2, Clip("clip2", testFile1))
        button2.performClick()

        assertFalse(button1.isPlaying)
        assertTrue(button2.isPlaying)
    }

    @Test
    fun `pausing activity releases media player`() {
        val testFile1 = File.createTempFile("audio1", ".mp3").absolutePath
        setupMediaPlayerDataSource(testFile1)
        val button = AudioButton(activity)
        audioHelper.setAudio(button, Clip("clip1", testFile1))
        activityController.pause()

        assertThat(
            Shadows.shadowOf(mediaPlayer).state,
            equalTo(ShadowMediaPlayer.State.END)
        )
    }

    @Test
    fun `pausing and resuming activity and then pressing play starts clip from the beginning`() {
        val testFile1 = File.createTempFile("audio1", ".mp3").absolutePath
        setupMediaPlayerDataSource(testFile1)
        val button = AudioButton(activity)
        audioHelper.setAudio(button, Clip("clip1", testFile1))
        button.performClick()
        Shadows.shadowOf(mediaPlayer).setCurrentPosition(1000)
        fakeScheduler.runForeground()
        activityController.pause()
        activityController.resume()
        button.performClick()

        assertThat(mediaPlayer.currentPosition, equalTo(0))
    }

    @Test
    fun `destroying lifecycle releases media player`() {
        val testFile1 = File.createTempFile("audio1", ".mp3").absolutePath
        setupMediaPlayerDataSource(testFile1)
        val button = AudioButton(activity)
        audioHelper.setAudio(button, Clip("clip1", testFile1))
        fakeLifecycleOwner.destroy()

        assertThat(
            Shadows.shadowOf(mediaPlayer).state,
            equalTo(ShadowMediaPlayer.State.END)
        )
    }

    @Test
    fun `setAudio() returns isPlaying state for button`() {
        val testFile1 = File.createTempFile("audio1", ".mp3").absolutePath
        setupMediaPlayerDataSource(testFile1)
        val button1 = AudioButton(activity)
        val isPlaying = audioHelper.setAudio(button1, Clip("clip1", testFile1))

        assertFalse(isPlaying.getOrAwaitValue())

        button1.performClick()

        assertTrue(isPlaying.getOrAwaitValue())

        button1.performClick()

        assertFalse(isPlaying.getOrAwaitValue())
    }
}
