package org.odk.collect.android.audio;

import android.media.MediaPlayer;

import androidx.lifecycle.LiveData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.odk.collect.android.support.FakeScheduler;
import org.odk.collect.android.support.LiveDataTester;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AudioPlayerViewModelTest {

    private final MediaPlayer mediaPlayer = mock(MediaPlayer.class);
    private final FakeScheduler fakeScheduler = new FakeScheduler();
    private final LiveDataTester liveDataTester = new LiveDataTester();

    private AudioPlayerViewModel viewModel;

    @Before
    public void setup() {
        viewModel = new AudioPlayerViewModel(() -> mediaPlayer, fakeScheduler);
    }

    @After
    public void teardown() {
        liveDataTester.teardown();
    }

    @Test
    public void play_resetsAndPreparesAndStartsMediaPlayer() throws Exception {
        viewModel.play("clip1", "file://audio.mp3");

        InOrder inOrder = Mockito.inOrder(mediaPlayer);

        inOrder.verify(mediaPlayer).reset();
        inOrder.verify(mediaPlayer).setDataSource("file://audio.mp3");
        inOrder.verify(mediaPlayer).prepare();
        inOrder.verify(mediaPlayer).start();
    }

    @Test
    public void play_whenAlreadyingPlayingClip_startsMediaPlayer() {
        viewModel.play("clip1", "file://audio.mp3");
        viewModel.play("clip1", "file://audio.mp3");

        verify(mediaPlayer, times(1)).reset();
        verify(mediaPlayer, times(2)).start();
    }

    @Test
    public void play_whenClipHasPositionSet_startsAtPosition() {
        viewModel.setPosition("clip1", 4321);
        viewModel.play("clip1", "file://audio.mp3");

        InOrder inOrder = Mockito.inOrder(mediaPlayer);

        inOrder.verify(mediaPlayer).seekTo(4321);
        inOrder.verify(mediaPlayer).start();
    }

    @Test
    public void isPlaying_whenNothingPlaying_is_false() {
        LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"));

        assertThat(isPlaying.getValue(), equalTo(false));
    }

    @Test
    public void isPlaying_whenClipIDPlaying_is_PLAYING() {
        LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"));

        viewModel.play("clip1", "file://audio.mp3");
        assertThat(isPlaying.getValue(), equalTo(true));
    }

    @Test
    public void isPlaying_whenDifferentClipIDPlaying_is_false() {
        LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("clip2"));

        viewModel.play("clip1", "file://other.mp3");
        assertThat(isPlaying.getValue(), equalTo(false));
    }

    @Test
    public void isPlaying_whenClipIDPlaying_thenCompleted_is_false() {
        final LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"));

        viewModel.play("clip1", "file://audio.mp3");

        ArgumentCaptor<MediaPlayer.OnCompletionListener> captor = ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener.class);
        verify(mediaPlayer).setOnCompletionListener(captor.capture());
        captor.getValue().onCompletion(mediaPlayer);

        assertThat(isPlaying.getValue(), equalTo(false));
    }

    @Test
    public void isPlaying_whenPlaybackFails_is_false() throws Exception {
        doThrow(IOException.class).when(mediaPlayer).setDataSource("file://missing.mp3");

        final LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"));
        viewModel.play("clip1", "file://missing.mp3");
        assertThat(isPlaying.getValue(), equalTo(false));
    }

    @Test
    public void stop_stopsMediaPlayer() {
        viewModel.play("clip1", "file://audio.mp3");
        viewModel.stop();
        verify(mediaPlayer).stop();
    }

    @Test
    public void stop_beforePlay_doesntCallStopOnMediaPlayer() {
        viewModel.stop();
        verify(mediaPlayer, never()).stop();
    }

    @Test
    public void stop_resetsPosition() {
        final LiveData<Integer> position = liveDataTester.activate(viewModel.getPosition("clip1"));

        viewModel.play("clip1", "file://audio.mp3");

        when(mediaPlayer.getCurrentPosition()).thenReturn(1000);
        fakeScheduler.runTask();

        viewModel.stop();
        assertThat(position.getValue(), equalTo(0));
    }

    @Test
    public void background_releasesMediaPlayer() {
        viewModel.background();
        verify(mediaPlayer).release();
    }

    @Test
    public void background_cancelsScheduler() {
        viewModel.background();
        assertThat(fakeScheduler.isCancelled(), equalTo(true));
    }

    @Test
    public void isPlaying_whenPlayingAndThenBackgrounding_is_false() {
        LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"));

        viewModel.play("clip1", "file://audio.mp3");
        viewModel.background();

        assertThat(isPlaying.getValue(), equalTo(false));
    }

    @Test
    public void play_afterBackground_createsANewMediaPlayer() {
        RecordingMockMediaPlayerFactory factory = new RecordingMockMediaPlayerFactory();
        AudioPlayerViewModel viewModel = new AudioPlayerViewModel(factory, new FakeScheduler());

        viewModel.play("clip1", "file://audio.mp3");
        assertThat(factory.createdInstances.size(), equalTo(1));
        verify(factory.createdInstances.get(0)).start();

        viewModel.background();
        viewModel.play("clip1", "file://audio.mp3");
        assertThat(factory.createdInstances.size(), equalTo(2));
        verify(factory.createdInstances.get(1)).start();
    }

    @Test
    public void pause_pausesMediaPlayer() {
        viewModel.pause();
        verify(mediaPlayer).pause();
    }

    @Test
    public void isPlaying_afterPause_is_false() {
        LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"));

        viewModel.play("clip1", "file://audio.mp3");
        viewModel.pause();

        assertThat(isPlaying.getValue(), equalTo(false));
    }

    @Test
    public void isPlaying_afterPause_andThenPlay_is_true() {
        final LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("clip1"));

        viewModel.play("clip1", "file://audio.mp3");
        viewModel.pause();
        viewModel.play("clip1", "file://audio.mp3");

        assertThat(isPlaying.getValue(), equalTo(true));
    }

    @Test
    public void getPosition_returnsMediaPlayerPositionInMilliseconds() {
        when(mediaPlayer.getCurrentPosition()).thenReturn(0);
        LiveData<Integer> duration = liveDataTester.activate(viewModel.getPosition("clip1"));
        assertThat(duration.getValue(), equalTo(0));

        viewModel.play("clip1", "file://audio.mp3");

        when(mediaPlayer.getCurrentPosition()).thenReturn(1000);
        fakeScheduler.runTask();
        assertThat(duration.getValue(), equalTo(1000));

        when(mediaPlayer.getCurrentPosition()).thenReturn(24135);
        fakeScheduler.runTask();
        assertThat(duration.getValue(), equalTo(24135));
    }

    @Test
    public void getPosition_worksWhenMultipleClipsArePlayed() {
        when(mediaPlayer.getCurrentPosition()).thenReturn(0);
        final LiveData<Integer> duration1 = liveDataTester.activate(viewModel.getPosition("clip1"));
        final LiveData<Integer> duration2 = liveDataTester.activate(viewModel.getPosition("clip2"));

        viewModel.play("clip1", "file://audio.mp3");
        when(mediaPlayer.getCurrentPosition()).thenReturn(1000);
        fakeScheduler.runTask();
        assertThat(duration1.getValue(), equalTo(1000));

        viewModel.play("clip2", "file://audio.mp3");
        when(mediaPlayer.getCurrentPosition()).thenReturn(2500);
        fakeScheduler.runTask();
        assertThat(duration2.getValue(), equalTo(2500));
    }

    @Test
    public void setPosition_whenClipIsPlaying_seeksMediaPlayer() {
        when(mediaPlayer.getDuration()).thenReturn(100000);
        viewModel.play("clip1", "file://audio.mp3");

        viewModel.setPosition("clip1", 54321);
        verify(mediaPlayer).seekTo(54321);
    }

    @Test
    public void setPosition_whenClipIsNotPlaying_doesNothing() {
        viewModel.setPosition("clip1", 54321);
        verify(mediaPlayer, never()).seekTo(anyInt());
    }

    @Test
    public void setPosition_updatesPosition() {
        LiveData<Integer> duration = liveDataTester.activate(viewModel.getPosition("clip1"));

        viewModel.setPosition("clip1", 54321);
        assertThat(duration.getValue(), equalTo(54321));
    }

    @Test
    public void onCleared_releasesMediaPlayer() {
        viewModel.onCleared();
        verify(mediaPlayer).release();
    }

    @Test
    public void onCleared_cancelsScheduler() {
        viewModel.onCleared();
        assertThat(fakeScheduler.isCancelled(), equalTo(true));
    }

    @Test
    public void whenPlaybackCompletes_cancelsScheduler() {
        viewModel.play("clip1", "file://audio.mp3");

        ArgumentCaptor<MediaPlayer.OnCompletionListener> captor = ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener.class);
        verify(mediaPlayer).setOnCompletionListener(captor.capture());
        captor.getValue().onCompletion(mediaPlayer);

        assertThat(fakeScheduler.isCancelled(), equalTo(true));
    }

    @Test
    public void whenPlaybackCompletes_resetsPosition() {
        final LiveData<Integer> position = liveDataTester.activate(viewModel.getPosition("clip1"));

        viewModel.play("clip1", "file://audio.mp3");

        when(mediaPlayer.getCurrentPosition()).thenReturn(1000);
        fakeScheduler.runTask();

        ArgumentCaptor<MediaPlayer.OnCompletionListener> captor = ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener.class);
        verify(mediaPlayer).setOnCompletionListener(captor.capture());
        captor.getValue().onCompletion(mediaPlayer);

        assertThat(position.getValue(), equalTo(0));
    }

    @Test
    public void error_whenPlaybackFails_is_PlaybackFailed() throws Exception {
        final LiveData<Exception> error = liveDataTester.activate(viewModel.getError());

        doThrow(IOException.class).when(mediaPlayer).setDataSource("file://missing.mp3");
        viewModel.play("clip1", "file://missing.mp3");

        assertThat(error.getValue(), equalTo(new PlaybackFailedException("file://missing.mp3")));
    }

    private static class RecordingMockMediaPlayerFactory implements MediaPlayerFactory {

        List<MediaPlayer> createdInstances = new ArrayList<>();

        @Override
        public MediaPlayer create() {
            MediaPlayer mock = mock(MediaPlayer.class);
            createdInstances.add(mock);

            return mock;
        }
    }
}