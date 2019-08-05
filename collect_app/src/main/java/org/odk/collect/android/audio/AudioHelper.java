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

        screenContext.getActivity().getLifecycle().addObserver(new AudioPlayerViewModelBackgroundObserver(viewModel));
        screenContext.getViewLifecycle().getLifecycle().addObserver(new AudioPlayerViewModelBackgroundObserver(viewModel));

        LiveData<Boolean> isPlaying = Transformations.map(viewModel.isPlaying(clipID), value -> value == PLAYING);

        isPlaying.observe(screenContext.getViewLifecycle(), button::setPlaying);
        button.setOnPlayStopListener(new AudioPlayerViewModelOnPlayStopListener(
                viewModel,
                uri,
                clipID
        ));

        return isPlaying;
    }

    public void play(String clipID, String uri) {
        getViewModel().play(clipID, uri);
    }

    @NotNull
    private AudioPlayerViewModel getViewModel() {
        return ViewModelProviders
                .of(screenContext.getActivity(), new AudioPlayerViewModelFactory(this.mediaPlayerFactory))
                .get(AudioPlayerViewModel.class);
    }
    
    private static class AudioPlayerViewModelOnPlayStopListener implements AudioButton.OnPlayStopListener {

        private final AudioPlayerViewModel viewModel;
        private final String uri;
        private final String buttonID;

        AudioPlayerViewModelOnPlayStopListener(AudioPlayerViewModel viewModel, String uri, String buttonID) {
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

    private static class AudioPlayerViewModelBackgroundObserver implements LifecycleObserver {

        private final AudioPlayerViewModel viewModel;

        AudioPlayerViewModelBackgroundObserver(AudioPlayerViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        void onPause() {
            viewModel.background();
        }
    }
}
