package org.odk.collect.android.audio;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

public final class AudioButtons {

    private AudioButtons() {}

    public static void setAudio(AudioButton button, String uri, FragmentActivity activity, MediaPlayerFactory mediaPlayerFactory) {
        AudioPlayerViewModel viewModel = ViewModelProviders
                .of(activity, new AudioPlayerViewModelFactory(mediaPlayerFactory))
                .get(AudioPlayerViewModel.class);

        viewModel.isPlaying(uri).observe(activity, button::setPlaying);
        button.setOnPlayStopListener(new AudioPlayerViewModelOnPlayStopListener(viewModel, uri));
    }

    private static class AudioPlayerViewModelOnPlayStopListener implements AudioButton.OnPlayStopListener {

        private final AudioPlayerViewModel viewModel;
        private final String uri;

        AudioPlayerViewModelOnPlayStopListener(AudioPlayerViewModel viewModel, String uri) {
            this.viewModel = viewModel;
            this.uri = uri;
        }

        @Override
        public void onPlayClicked() {
            viewModel.play(uri);
        }

        @Override
        public void onStopClicked() {
            viewModel.stop();
        }
    }
}
