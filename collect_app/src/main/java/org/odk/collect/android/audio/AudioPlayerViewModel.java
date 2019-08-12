package org.odk.collect.android.audio;

import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.io.IOException;

import static org.odk.collect.android.audio.AudioPlayerViewModel.ClipState.NOT_PLAYING;
import static org.odk.collect.android.audio.AudioPlayerViewModel.ClipState.PAUSED;
import static org.odk.collect.android.audio.AudioPlayerViewModel.ClipState.PLAYING;

class AudioPlayerViewModel extends ViewModel implements MediaPlayer.OnCompletionListener {

    private final MediaPlayerFactory mediaPlayerFactory;
    private final Scheduler scheduler;
    private MediaPlayer mediaPlayer;

    private final MutableLiveData<CurrentlyPlaying> currentlyPlaying = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>();

    private Boolean scheduledDurationUpdates = false;

    public enum ClipState {
        NOT_PLAYING,
        PLAYING,
        PAUSED
    }

    AudioPlayerViewModel(MediaPlayerFactory mediaPlayerFactory, Scheduler scheduler) {
        this.mediaPlayerFactory = mediaPlayerFactory;
        this.scheduler = scheduler;

        currentlyPlaying.setValue(null);
    }

    public void play(String clipID, String uri) {
        if (!isCurrentPlayingClip(clipID, currentlyPlaying.getValue())) {
            loadClip(uri);
        }

        getMediaPlayer().start();
        currentlyPlaying.setValue(new CurrentlyPlaying(clipID, false));
    }

    private void loadClip(String uri) {
        try {
            getMediaPlayer().reset();
            getMediaPlayer().setDataSource(uri);
            getMediaPlayer().prepare();
        } catch (IOException ignored) {
            throw new RuntimeException();
        }
    }

    public void stop() {
        getMediaPlayer().stop();
        currentlyPlaying.setValue(null);
    }

    public void pause() {
        getMediaPlayer().pause();

        CurrentlyPlaying currentlyPlayingValue = currentlyPlaying.getValue();
        if (currentlyPlayingValue != null) {
            currentlyPlaying.setValue(currentlyPlayingValue.paused());
        }
    }

    public LiveData<ClipState> isPlaying(@NonNull String clipID) {
        return Transformations.map(currentlyPlaying, value -> {
            if (isCurrentPlayingClip(clipID, value)) {
                if (value.isPaused()) {
                    return PAUSED;
                } else {
                    return PLAYING;
                }
            } else {
                return NOT_PLAYING;
            }
        });
    }

    public LiveData<Integer> getPosition() {
        currentPosition.setValue(getMediaPlayer().getCurrentPosition() / 1000);
        schedulePositionUpdates();

        return currentPosition;
    }

    public void background() {
        releaseMediaPlayer();
        currentlyPlaying.setValue(null);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        currentlyPlaying.setValue(null);
    }

    @Override
    protected void onCleared() {
        releaseMediaPlayer();
        scheduler.cancel();
    }

    private void schedulePositionUpdates() {
        if (!scheduledDurationUpdates) {
            scheduler.schedule(() -> currentPosition.postValue(getMediaPlayer().getCurrentPosition() / 1000), 500);
            scheduledDurationUpdates = true;
        }
    }

    private void releaseMediaPlayer() {
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

    private boolean isCurrentPlayingClip(String clipID, CurrentlyPlaying currentlyPlayingValue) {
        return currentlyPlayingValue != null && currentlyPlayingValue.clipID.equals(clipID);
    }

    private static class CurrentlyPlaying {

        private final String clipID;
        private final boolean paused;

        CurrentlyPlaying(String clipID, boolean paused) {
            this.clipID = clipID;
            this.paused = paused;
        }

        String getClipID() {
            return clipID;
        }

        boolean isPaused() {
            return paused;
        }

        CurrentlyPlaying paused() {
            return new CurrentlyPlaying(clipID, true);
        }
    }
}
