package org.odk.collect.android.audio;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

public final class AudioButtons {

    private AudioButtons() {
    }

    public static void setAudio(AudioButton button, String uri, FragmentActivity activity, MediaPlayerFactory mediaPlayerFactory) {
        AudioPlayerViewModel viewModel = ViewModelProviders
                .of(activity, new AudioPlayerViewModelFactory(mediaPlayerFactory))
                .get(AudioPlayerViewModel.class);

        String buttonID = String.valueOf(ViewCompat.generateViewId());

        viewModel.isPlaying(buttonID).observe(activity, button::setPlaying);
        button.setOnPlayStopListener(new AudioPlayerViewModelOnPlayStopListener(
                viewModel,
                uri,
                buttonID
        ));
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
}
