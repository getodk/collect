package org.odk.collect.android.audio;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class AudioPlayerViewModelFactory implements ViewModelProvider.Factory {

    private final MediaPlayerFactory mediaPlayerFactory;

    public AudioPlayerViewModelFactory(MediaPlayerFactory mediaPlayerFactory) {
        this.mediaPlayerFactory = mediaPlayerFactory;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AudioPlayerViewModel(mediaPlayerFactory);
    }
}
