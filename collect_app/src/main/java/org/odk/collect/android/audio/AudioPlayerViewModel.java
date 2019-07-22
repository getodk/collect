package org.odk.collect.android.audio;

import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.io.IOException;

class AudioPlayerViewModel extends ViewModel implements MediaPlayer.OnCompletionListener {

    private final MediaPlayerFactory mediaPlayerFactory;
    private MediaPlayer mediaPlayer;

    private final MutableLiveData<String> playingURI = new MutableLiveData<>();

    AudioPlayerViewModel(MediaPlayerFactory mediaPlayerFactory) {
        this.mediaPlayerFactory = mediaPlayerFactory;

        playingURI.setValue(null);
    }

    public void play(String uri) {
        try {
            getMediaPlayer().reset();
            getMediaPlayer().setDataSource(uri);
            getMediaPlayer().prepare();
            getMediaPlayer().start();
            playingURI.setValue(uri);
        } catch (IOException ignored) {
            throw new RuntimeException();
        }
    }

    public void stop() {
        getMediaPlayer().stop();
        playingURI.setValue(null);
    }

    public LiveData<Boolean> isPlaying(@NonNull String uri) {
        return Transformations.map(playingURI, playingURI -> uri.equals(playingURI));
    }

    public void background() {
        release();
        playingURI.setValue(null);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playingURI.setValue(null);
    }

    @Override
    protected void onCleared() {
        release();
    }

    private void release() {
        getMediaPlayer().release();
        mediaPlayer = null;
    }

    private MediaPlayer getMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = mediaPlayerFactory.create();
            mediaPlayer.setOnCompletionListener(this);
        }

        return mediaPlayer;
    }
}
