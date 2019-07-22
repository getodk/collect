package org.odk.collect.android.audio;

import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class AudioPlayerViewModelTest {

    private final MediaPlayer mediaPlayer = mock(MediaPlayer.class);
    private final LiveDataTester liveDataTester = new LiveDataTester();

    private AudioPlayerViewModel viewModel;

    @Before
    public void setup() {
        viewModel = new AudioPlayerViewModel(RuntimeEnvironment.application, () -> mediaPlayer);
    }

    @After
    public void teardown() {
        liveDataTester.teardown();
    }

    @Test
    public void isPlaying_whenNothingPlaying_returnsFalse() {
        LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("file://audio.mp3"));

        assertThat(isPlaying.getValue(), is(false));
    }

    @Test
    public void isPlaying_whenURIPlaying_returnsTrue() {
        LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("file://audio.mp3"));

        viewModel.play("file://audio.mp3");
        assertThat(isPlaying.getValue(), is(true));
    }

    @Test
    public void isPlaying_whenDifferentURIPlaying_returnsFalse() {
        LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("file://audio.mp3"));

        viewModel.play("file://other.mp3");
        assertThat(isPlaying.getValue(), is(false));
    }

    @Test
    public void isPlaying_whenURIPlaying_thenPaused_returnsFalse() {
        LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("file://audio.mp3"));

        viewModel.play("file://audio.mp3");
        viewModel.stop();

        assertThat(isPlaying.getValue(), is(false));
    }

    @Test
    public void isPlaying_whenURIPlaying_thenCompleted_returnsFalse() {
        final LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("file://audio.mp3"));

        viewModel.play("file://audio.mp3");

        ArgumentCaptor<MediaPlayer.OnCompletionListener> captor = ArgumentCaptor.forClass(MediaPlayer.OnCompletionListener.class);
        verify(mediaPlayer).setOnCompletionListener(captor.capture());
        captor.getValue().onCompletion(mediaPlayer);

        assertThat(isPlaying.getValue(), is(false));
    }

    @Test
    public void isPlaying_whenPlayingAndThenBackgrounding_returnsFalse() {
        LiveData<Boolean> isPlaying = liveDataTester.activate(viewModel.isPlaying("file://audio.mp3"));

        viewModel.play("file://audio.mp3");
        viewModel.background();

        assertThat(isPlaying.getValue(), is(false));
    }

    @Test
    public void play_resetsAndPreparesAndStartsMediaPlayer() throws Exception {
        viewModel.play("file://audio.mp3");

        InOrder inOrder = Mockito.inOrder(mediaPlayer);

        inOrder.verify(mediaPlayer).reset();
        inOrder.verify(mediaPlayer).setDataSource(RuntimeEnvironment.application, Uri.parse("file://audio.mp3"));
        inOrder.verify(mediaPlayer).prepare();
        inOrder.verify(mediaPlayer).start();
    }

    @Test
    public void play_afterBackground_createsANewMediaPlayer() {
        RecordingMockMediaPlayerFactory factory = new RecordingMockMediaPlayerFactory();
        AudioPlayerViewModel viewModel = new AudioPlayerViewModel(RuntimeEnvironment.application, factory);

        viewModel.play("file://audio.mp3");
        assertThat(factory.createdInstances.size(), equalTo(1));
        verify(factory.createdInstances.get(0)).start();

        viewModel.background();
        viewModel.play("file://audio.mp3");
        assertThat(factory.createdInstances.size(), equalTo(2));
        verify(factory.createdInstances.get(1)).start();
    }

    @Test
    public void stop_stopsMediaPlayer() {
        viewModel.stop();
        verify(mediaPlayer).stop();
    }

    @Test
    public void background_releasesMediaPlayer() {
        viewModel.background();
        verify(mediaPlayer).release();
    }

    @Test
    public void onCleared_releasesMediaPlayer() {
        viewModel.onCleared();
        verify(mediaPlayer).release();
    }

    private static class RecordingMockMediaPlayerFactory implements AudioPlayerViewModel.MediaPlayerFactory {

        public List<MediaPlayer> createdInstances = new ArrayList<>();

        @Override
        public MediaPlayer create() {
            MediaPlayer mock = mock(MediaPlayer.class);
            createdInstances.add(mock);

            return mock;
        }
    }

    private static class LiveDataTester {

        private final FakeLifecycleOwner owner = new FakeLifecycleOwner();

        public <T> LiveData<T> activate(LiveData<T> liveData) {
            liveData.observe(owner, (Observer<Object>) any -> { });
            return liveData;
        }

        public void teardown() {
            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        }

        private static class FakeLifecycleOwner implements LifecycleOwner {

            private final LifecycleRegistry lifecycle = new LifecycleRegistry(this);

            FakeLifecycleOwner() {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
            }

            @NonNull
            @Override
            public Lifecycle getLifecycle() {
                return lifecycle;
            }
        }
    }
}