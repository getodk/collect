package org.odk.collect.android.audio;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import androidx.core.util.Pair;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;

import org.jetbrains.annotations.NotNull;

import static org.odk.collect.android.audio.AudioPlayerViewModel.ClipState;
import static org.odk.collect.android.audio.AudioPlayerViewModel.ClipState.PLAYING;

public class AudioHelper {

    private final ScreenContext screenContext;
    private final MediaPlayerFactory mediaPlayerFactory;
    private final Scheduler scheduler;

    public AudioHelper(ScreenContext screenContext, MediaPlayerFactory mediaPlayerFactory, Scheduler scheduler) {
        this.screenContext = screenContext;
        this.mediaPlayerFactory = mediaPlayerFactory;
        this.scheduler = scheduler;
    }

    public AudioHelper(ScreenContext screenContext) {
        this(screenContext, MediaPlayer::new, new TimerScheduler());
    }

    public LiveData<Boolean> setAudio(AudioButton button, String uri, String clipID) {
        AudioPlayerViewModel viewModel = getViewModel();

        LiveData<Boolean> isPlaying = Transformations.map(viewModel.isPlaying(clipID), value -> value == PLAYING);

        isPlaying.observe(screenContext.getViewLifecycle(), button::setPlaying);
        button.setListener(new AudioButtonListener(viewModel, uri, clipID));

        return isPlaying;
    }

    public void setAudio(AudioControllerView view, String uri, String clipID) {
        AudioPlayerViewModel viewModel = getViewModel();
        LifecycleOwner lifecycle = screenContext.getViewLifecycle();

        view.setDuration(getDurationOfFile(uri));

        LiveData<ClipState> playState = viewModel.isPlaying(clipID);
        playState.observe(lifecycle, view::setPlayState);

        LiveData<Integer> position = viewModel.getPosition();
        LiveDataZipper<ClipState, Integer> liveDataZipper = new LiveDataZipper<>(playState, position);

        liveDataZipper.zip().observe(lifecycle, (playStateAndPosition) -> {
            if (playStateAndPosition.first == PLAYING) {
                view.setPosition(playStateAndPosition.second);
            } else {
                view.setPosition(0);
            }
        });

        view.setListener(new AudioControllerViewListener(viewModel, uri, clipID));
    }

    private int getDurationOfFile(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Integer.parseInt(durationString) / 1000;
    }

    public void play(String clipID, String uri) {
        getViewModel().play(clipID, uri);
    }

    @NotNull
    private AudioPlayerViewModel getViewModel() {
        AudioPlayerViewModelFactory factory = new AudioPlayerViewModelFactory(mediaPlayerFactory, scheduler);

        AudioPlayerViewModel viewModel = ViewModelProviders
                .of(screenContext.getActivity(), factory)
                .get(AudioPlayerViewModel.class);

        screenContext.getActivity().getLifecycle().addObserver(new BackgroundObserver(viewModel));
        screenContext.getViewLifecycle().getLifecycle().addObserver(new BackgroundObserver(viewModel));

        return viewModel;
    }

    private static class AudioControllerViewListener implements AudioControllerView.Listener {

        private final AudioPlayerViewModel viewModel;
        private final String uri;
        private final String clipID;

        AudioControllerViewListener(AudioPlayerViewModel viewModel, String uri, String clipID) {
            this.viewModel = viewModel;
            this.uri = uri;
            this.clipID = clipID;
        }

        @Override
        public void onPlayClicked() {
            viewModel.play(clipID, uri);
        }

        @Override
        public void onPauseClicked() {
            viewModel.pause();
        }
    }

    private static class AudioButtonListener implements AudioButton.Listener {

        private final AudioPlayerViewModel viewModel;
        private final String uri;
        private final String buttonID;

        AudioButtonListener(AudioPlayerViewModel viewModel, String uri, String buttonID) {
            this.viewModel = viewModel;
            this.uri = uri;
            this.buttonID = buttonID;
        }

        @Override
        public void onPlayClicked() {
            viewModel.play(buttonID, uri);
        }

        @Override
        public void onStopClicked() {
            viewModel.stop();
        }
    }

    private static class BackgroundObserver implements LifecycleObserver {

        private final AudioPlayerViewModel viewModel;

        BackgroundObserver(AudioPlayerViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        void onPause() {
            viewModel.background();
        }
    }

    private class LiveDataZipper<A, B> {

        private final LiveData<A> liveDataOne;
        private final LiveData<B> liveDataTwo;

        private A one;
        private B two;

        LiveDataZipper(LiveData<A> liveDataOne, LiveData<B> liveDataTwo) {
            this.liveDataOne = liveDataOne;
            this.liveDataTwo = liveDataTwo;
        }

        public LiveData<Pair<A, B>> zip() {
            MediatorLiveData<Pair<A, B>> mediatorLiveData = new MediatorLiveData<>();

            mediatorLiveData.addSource(liveDataOne, (value) -> {
                one = value;
                mediatorLiveData.setValue(new Pair<>(one, two));
            });

            mediatorLiveData.addSource(liveDataTwo, (value) -> {
                two = value;
                mediatorLiveData.setValue(new Pair<>(one, two));
            });

            return mediatorLiveData;
        }
    }
}
