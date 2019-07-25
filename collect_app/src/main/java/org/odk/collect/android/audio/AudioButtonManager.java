package org.odk.collect.android.audio;

import androidx.core.view.ViewCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;

import static org.odk.collect.android.audio.AudioPlayerViewModel.ClipState.PLAYING;

public class AudioButtonManager {

    private final AndroidScreen screen;

    public AudioButtonManager(AndroidScreen screen) {
        this.screen = screen;
    }

    public LiveData<Boolean> setAudio(AudioButton button, String uri, MediaPlayerFactory mediaPlayerFactory) {
        AudioPlayerViewModel viewModel = ViewModelProviders
                .of(screen.getActivity(), new AudioPlayerViewModelFactory(mediaPlayerFactory))
                .get(AudioPlayerViewModel.class);

        screen.getActivity().getLifecycle().addObserver(new AudioPlayerViewModelBackgroundObserver(viewModel));

        String buttonID = String.valueOf(ViewCompat.generateViewId());
        LiveData<Boolean> isPlaying = Transformations.map(viewModel.isPlaying(buttonID), value -> value == PLAYING);

        isPlaying.observe(screen.getActivity(), button::setPlaying);
        button.setOnPlayStopListener(new AudioPlayerViewModelOnPlayStopListener(
                viewModel,
                uri,
                buttonID
        ));

        return isPlaying;
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
