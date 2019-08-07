package org.odk.collect.android.audio;

import android.media.MediaPlayer;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;

import org.jetbrains.annotations.NotNull;

import static org.odk.collect.android.audio.AudioPlayerViewModel.ClipState.PLAYING;

public class AudioHelper {

    private final ScreenContext screenContext;
    private final MediaPlayerFactory mediaPlayerFactory;

    public AudioHelper(ScreenContext screenContext, MediaPlayerFactory mediaPlayerFactory) {
        this.screenContext = screenContext;
        this.mediaPlayerFactory = mediaPlayerFactory;
    }

    public AudioHelper(ScreenContext screenContext) {
        this(screenContext, MediaPlayer::new);
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

        LiveData<AudioPlayerViewModel.ClipState> playState = viewModel.isPlaying(clipID);
        playState.observe(screenContext.getViewLifecycle(), view::setPlayState);
        view.setListener(new AudioControllerViewListener(viewModel, uri, clipID));
    }

    public void play(String clipID, String uri) {
        getViewModel().play(clipID, uri);
    }

    @NotNull
    private AudioPlayerViewModel getViewModel() {
        AudioPlayerViewModel viewModel = ViewModelProviders
                .of(screenContext.getActivity(), new AudioPlayerViewModelFactory(this.mediaPlayerFactory))
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
}
