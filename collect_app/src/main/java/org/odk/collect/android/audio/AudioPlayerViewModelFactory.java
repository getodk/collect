package org.odk.collect.android.audio;

import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.async.Scheduler;

import java.util.function.Supplier;

class AudioPlayerViewModelFactory implements ViewModelProvider.Factory {

    private final Supplier<MediaPlayer> mediaPlayerFactory;
    private final Scheduler scheduler;

    AudioPlayerViewModelFactory(Supplier<MediaPlayer> mediaPlayerFactory, Scheduler scheduler) {
        this.mediaPlayerFactory = mediaPlayerFactory;
        this.scheduler = scheduler;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AudioPlayerViewModel(mediaPlayerFactory, scheduler);
    }
}
