package org.odk.collect.audioclips

import android.media.MediaPlayer
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.odk.collect.androidtest.LiveDataTester
import org.odk.collect.testshared.FakeScheduler
import java.io.File
import java.io.IOException
import java.util.function.Supplier

class AudioClipViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mediaPlayer = mock(MediaPlayer::class.java)
    private val fakeScheduler = FakeScheduler()
    private val liveDataTester = LiveDataTester()
    private var viewModel: AudioClipViewModel = AudioClipViewModel(Supplier { mediaPlayer }, fakeScheduler)

    @After
    fun teardown() {
        liveDataTester.teardown()
    }

    @Test
    fun play_resetsAndPreparesAndStartsMediaPlayer() {
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        val inOrder = inOrder(mediaPlayer)
        inOrder.verify(mediaPlayer).reset()
        inOrder.verify(mediaPlayer).setDataSource("file://audio.mp3")
        inOrder.verify(mediaPlayer).prepare()
        inOrder.verify(mediaPlayer).start()
    }

    @Test
    fun play_whenAlreadyingPlayingClip_startsMediaPlayer() {
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        verify(mediaPlayer, times(1)).reset()
        verify(mediaPlayer, times(2)).start()
    }

    @Test
    fun play_whenClipHasPositionSet_startsAtPosition() {
        viewModel.setPosition("clip1", 4321)
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        val inOrder = inOrder(mediaPlayer)
        inOrder.verify(mediaPlayer).seekTo(4321)
        inOrder.verify(mediaPlayer).start()
    }

    @Test
    fun playMultipleClips_updatesProgress_forAllClips() {
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        assertThat(fakeScheduler.isRepeatRunning(), equalTo(true))
        viewModel.onCleared()
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        assertThat(fakeScheduler.isRepeatRunning(), equalTo(true))
    }

    @Test
    fun playInOrder_playsClipsOneAfterTheOther_andUpdatesProgress() {
        viewModel.playInOrder(
            listOf(
                Clip("clip1", "file://audio1.mp3"),
                Clip("clip2", "file://audio2.mp3")
            )
        )

        val captor = ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener::class.java)
        verify(mediaPlayer).setOnCompletionListener(captor.capture())
        val onCompletionListener = captor.value
        verify(mediaPlayer).setDataSource("file://audio1.mp3")
        verify(mediaPlayer, times(1)).start()
        assertThat(fakeScheduler.isRepeatRunning(), equalTo(true))
        onCompletionListener.onCompletion(mediaPlayer)
        verify(mediaPlayer).setDataSource("file://audio2.mp3")
        verify(mediaPlayer, times(2)).start()
        assertThat(fakeScheduler.isRepeatRunning(), equalTo(true))
        onCompletionListener.onCompletion(mediaPlayer)
        verify(mediaPlayer, times(2)).start()
        assertThat(fakeScheduler.isRepeatRunning(), equalTo(false))
    }

    @Test
    fun playInOrder_whenThereIsAnErrorContinuesWithNextClip() {
        doThrow(IOException::class.java).`when`(mediaPlayer).setDataSource("file://missing.mp3")
        viewModel.playInOrder(
            listOf(
                Clip("clip1", "file://missing.mp3"),
                Clip("clip2", "file://not-missing.mp3")
            )
        )

        verify(mediaPlayer).setDataSource("file://not-missing.mp3")
        verify(mediaPlayer, times(1)).start()
    }

    @Test
    fun play_afterAPlayInOrder_doesNotContinuePlayingClips() {
        viewModel.playInOrder(
            listOf(
                Clip("clip1", "file://audio1.mp3"),
                Clip("clip2", "file://audio2.mp3")
            )
        )

        viewModel.play(Clip("clip3", "file://audio3.mp3"))
        verify(mediaPlayer, times(2)).start()
        verify(mediaPlayer).setDataSource("file://audio1.mp3")
        verify(mediaPlayer).setDataSource("file://audio3.mp3")
        val captor = ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener::class.java)
        verify(mediaPlayer).setOnCompletionListener(captor.capture())
        val onCompletionListener = captor.value
        onCompletionListener.onCompletion(mediaPlayer)
        verify(mediaPlayer, never()).setDataSource("file://audio2.mp3")
        verify(mediaPlayer, times(2)).start()
    }

    @Test
    fun isPlaying_whenNothingPlaying_is_false() {
        val isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"))
        assertThat(isPlaying.value, equalTo(false))
    }

    @Test
    fun isPlaying_whenClipIDPlaying_is_PLAYING() {
        val isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"))
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        assertThat(isPlaying.value, equalTo(true))
    }

    @Test
    fun isPlaying_whenDifferentClipIDPlaying_is_false() {
        val isPlaying = liveDataTester.activate(viewModel.isPlaying("clip2"))
        viewModel.play(Clip("clip1", "file://other.mp3"))
        assertThat(isPlaying.value, equalTo(false))
    }

    @Test
    fun isPlaying_whenClipIDPlaying_thenCompleted_is_false() {
        val isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"))
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        val captor = ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener::class.java)
        verify(mediaPlayer).setOnCompletionListener(captor.capture())
        captor.value.onCompletion(mediaPlayer)
        assertThat(isPlaying.value, equalTo(false))
    }

    @Test
    fun isPlaying_whenPlaybackFails_is_false() {
        doThrow(IOException::class.java).`when`(mediaPlayer).setDataSource("file://missing.mp3")
        val isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"))
        viewModel.play(Clip("clip1", "file://missing.mp3"))
        assertThat(isPlaying.value, equalTo(false))
    }

    @Test
    fun stop_stopsMediaPlayer() {
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        viewModel.stop()
        verify(mediaPlayer).stop()
    }

    @Test
    fun stop_beforePlay_doesntCallStopOnMediaPlayer() {
        viewModel.stop()
        verify(mediaPlayer, never()).stop()
    }

    @Test
    fun stop_resetsPosition() {
        val position = liveDataTester.activate(viewModel.getPosition("clip1"))
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        `when`(mediaPlayer.currentPosition).thenReturn(1000)
        fakeScheduler.runForeground()
        viewModel.stop()
        assertThat(position.value, equalTo(0))
    }

    @Test
    fun background_releasesMediaPlayer() {
        viewModel.background()
        verify(mediaPlayer).release()
    }

    @Test
    fun background_cancelsScheduler() {
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        viewModel.background()
        assertThat(fakeScheduler.isRepeatRunning(), equalTo(false))
    }

    @Test
    fun isPlaying_whenPlayingAndThenBackgrounding_is_false() {
        val isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"))
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        viewModel.background()
        assertThat(isPlaying.value, equalTo(false))
    }

    @Test
    fun play_afterBackground_createsANewMediaPlayer() {
        val factory = RecordingMockMediaPlayerFactory()
        val viewModel = AudioClipViewModel(factory, FakeScheduler())
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        assertThat(factory.createdInstances.size, equalTo(1))
        verify(factory.createdInstances[0]).start()
        viewModel.background()
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        assertThat(factory.createdInstances.size, equalTo(2))
        verify(factory.createdInstances[1]).start()
    }

    @Test
    fun pause_pausesMediaPlayer() {
        viewModel.pause()
        verify(mediaPlayer).pause()
    }

    @Test
    fun isPlaying_afterPause_is_false() {
        val isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"))
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        viewModel.pause()
        assertThat(isPlaying.value, equalTo(false))
    }

    @Test
    fun isPlaying_afterPause_andThenPlay_is_true() {
        val isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"))
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        viewModel.pause()
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        assertThat(isPlaying.value, equalTo(true))
    }

    @Test
    fun position_returnsMediaPlayerPositionInMilliseconds() {
        `when`(mediaPlayer.currentPosition).thenReturn(0)
        val position = liveDataTester.activate(viewModel.getPosition("clip1"))
        assertThat(position.value, equalTo(0))

        viewModel.play(Clip("clip1", "file://audio.mp3"))
        `when`(mediaPlayer.currentPosition).thenReturn(1000)
        fakeScheduler.runForeground()
        assertThat(position.value, equalTo(1000))

        `when`(mediaPlayer.currentPosition).thenReturn(24135)
        fakeScheduler.runForeground()
        assertThat(position.value, equalTo(24135))
    }

    @Test
    fun position_worksWhenMultipleClipsArePlayed() {
        `when`(mediaPlayer.currentPosition).thenReturn(0)
        val position1 = liveDataTester.activate(viewModel.getPosition("clip1"))
        val position2 = liveDataTester.activate(viewModel.getPosition("clip2"))

        viewModel.play(Clip("clip1", "file://audio.mp3"))
        `when`(mediaPlayer.currentPosition).thenReturn(1000)
        fakeScheduler.runForeground()
        assertThat(position1.value, equalTo(1000))
        assertThat(position2.value, equalTo(0))

        viewModel.play(Clip("clip2", "file://audio.mp3"))
        `when`(mediaPlayer.currentPosition).thenReturn(2500)
        fakeScheduler.runForeground()
        assertThat(position1.value, equalTo(1000))
        assertThat(position2.value, equalTo(2500))
    }

    @Test
    fun setPosition_whenClipIsPlaying_seeksMediaPlayer() {
        `when`(mediaPlayer.duration).thenReturn(100000)
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        viewModel.setPosition("clip1", 54321)
        verify(mediaPlayer).seekTo(54321)
    }

    @Test
    fun setPosition_whenClipIsNotPlaying_doesNothing() {
        viewModel.setPosition("clip1", 54321)
        verify(mediaPlayer, never()).seekTo(ArgumentMatchers.anyInt())
    }

    @Test
    fun setPosition_updatesPosition() {
        val duration = liveDataTester.activate(viewModel.getPosition("clip1"))
        viewModel.setPosition("clip1", 54321)
        assertThat(duration.value, equalTo(54321))
    }

    @Test
    fun onCleared_releasesMediaPlayer() {
        viewModel.onCleared()
        verify(mediaPlayer).release()
    }

    @Test
    fun onCleared_cancelsScheduler() {
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        viewModel.onCleared()
        assertThat(fakeScheduler.isRepeatRunning(), equalTo(false))
    }

    @Test
    fun whenPlaybackCompletes_cancelsScheduler() {
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        val captor = ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener::class.java)
        verify(mediaPlayer).setOnCompletionListener(captor.capture())
        captor.value.onCompletion(mediaPlayer)
        assertThat(fakeScheduler.isRepeatRunning(), equalTo(false))
    }

    @Test
    fun whenPlaybackCompletes_resetsPosition() {
        val position = liveDataTester.activate(viewModel.getPosition("clip1"))
        viewModel.play(Clip("clip1", "file://audio.mp3"))
        `when`(mediaPlayer.currentPosition).thenReturn(1000)
        fakeScheduler.runForeground()
        val captor = ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener::class.java)
        verify(mediaPlayer).setOnCompletionListener(captor.capture())
        captor.value.onCompletion(mediaPlayer)
        assertThat(position.value, equalTo(0))
    }

    @Test
    fun error_whenPlaybackFailsBecauseOfMissingFile_is_PlaybackFailed() {
        val error = liveDataTester.activate(viewModel.getError())
        doThrow(IOException::class.java).`when`(mediaPlayer).setDataSource("file://missing.mp3")
        viewModel.play(Clip("clip1", "file://missing.mp3"))
        assertThat(error.value, equalTo<Exception?>(PlaybackFailedException("file://missing.mp3", 0)))
    }

    @Test
    fun error_whenPlaybackFailsBecauseOfInvalidFile_is_PlaybackFailed() {
        val error = liveDataTester.activate(viewModel.getError())
        val invalid = File.createTempFile("invalid", ".mp3")
        doThrow(IOException::class.java).`when`(mediaPlayer).setDataSource(invalid.absolutePath)
        viewModel.play(Clip("clip1", invalid.absolutePath))
        assertThat(error.value, equalTo<Exception?>(PlaybackFailedException(invalid.absolutePath, 1)))
    }

    @Test
    fun dismissError_removesErrorValue() {
        val error = liveDataTester.activate(viewModel.getError())
        doThrow(IOException::class.java).`when`(mediaPlayer).setDataSource("file://missing.mp3")
        viewModel.play(Clip("clip1", "file://missing.mp3"))
        viewModel.errorDisplayed()
        assertThat(error.value, equalTo<Exception?>(null))
    }

    private class RecordingMockMediaPlayerFactory : Supplier<MediaPlayer> {
        var createdInstances: MutableList<MediaPlayer> = ArrayList()
        override fun get(): MediaPlayer {
            val mock = mock(MediaPlayer::class.java)
            createdInstances.add(mock)
            return mock
        }
    }
}
