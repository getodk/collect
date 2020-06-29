package org.odk.collect.android.audio;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.async.Scheduler;

class AudioPlayerViewModelFactory implements ViewModelProvider.Factory {

    private final MediaPlayerFactory mediaPlayerFactory;
    private final Scheduler scheduler;

    AudioPlayerViewModelFactory(MediaPlayerFactory mediaPlayerFactory, Scheduler scheduler) {
        this.mediaPlayerFactory = mediaPlayerFactory;
        this.scheduler = scheduler;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AudioPlayerViewModel(mediaPlayerFactory, scheduler);
    }
}
