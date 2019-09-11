package org.odk.collect.android.audio;

import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.odk.collect.android.utilities.Scheduler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class AudioPlayerViewModel extends ViewModel implements MediaPlayer.OnCompletionListener {

    private final MediaPlayerFactory mediaPlayerFactory;
    private final Scheduler scheduler;
    private MediaPlayer mediaPlayer;

    private final MutableLiveData<CurrentlyPlaying> currentlyPlaying = new MutableLiveData<>();
    private final MutableLiveData<Exception> error = new MutableLiveData<>();
    private final Map<String, MutableLiveData<Integer>> positions = new HashMap<>();

    private Boolean scheduledDurationUpdates = false;

    AudioPlayerViewModel(MediaPlayerFactory mediaPlayerFactory, Scheduler scheduler) {
        this.mediaPlayerFactory = mediaPlayerFactory;
        this.scheduler = scheduler;

        currentlyPlaying.setValue(null);
    }

    public void play(String clipID, String uri) {
        if (!isCurrentPlayingClip(clipID, currentlyPlaying.getValue())) {
            try {
                loadNewClip(uri);
            } catch (IOException ignored) {
                error.setValue(new PlaybackFailedException(uri));
                return;
            }
        }

        getMediaPlayer().seekTo(getPositionForClip(clipID).getValue());
        getMediaPlayer().start();

        currentlyPlaying.setValue(new CurrentlyPlaying(clipID, false));
        schedulePositionUpdates();
    }

    public void stop() {
        if (currentlyPlaying.getValue() != null) {
            getMediaPlayer().stop();
        }

        resetCurrentlyPlaying();
        clearCurrentlyPlaying();
    }

    public void pause() {
        getMediaPlayer().pause();

        CurrentlyPlaying currentlyPlayingValue = currentlyPlaying.getValue();
        if (currentlyPlayingValue != null) {
            currentlyPlaying.setValue(currentlyPlayingValue.paused());
        }
    }

    public LiveData<Boolean> isPlaying(@NonNull String clipID) {
        return Transformations.map(currentlyPlaying, value -> {
            if (isCurrentPlayingClip(clipID, value)) {
                return !value.isPaused();
            } else {
                return false;
            }
        });
    }

    public LiveData<Integer> getPosition(String clipID) {
        return getPositionForClip(clipID);
    }

    public void setPosition(String clipID, Integer newPosition) {
        if (isCurrentPlayingClip(clipID, currentlyPlaying.getValue())) {
            getMediaPlayer().seekTo(newPosition);
        }

        getPositionForClip(clipID).setValue(newPosition);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        resetCurrentlyPlaying();
        clearCurrentlyPlaying();
    }

    public void background() {
        resetCurrentlyPlaying();
        clearCurrentlyPlaying();
        releaseMediaPlayer();
    }

    @Override
    protected void onCleared() {
        cancelPositionUpdates();
        releaseMediaPlayer();
    }

    private void resetCurrentlyPlaying() {
        CurrentlyPlaying currentlyPlayingValue = currentlyPlaying.getValue();

        if (currentlyPlayingValue != null) {
            getPositionForClip(currentlyPlayingValue.getClipID()).setValue(0);
        }
    }

    private void clearCurrentlyPlaying() {
        cancelPositionUpdates();
        currentlyPlaying.setValue(null);
    }

    @NonNull
    private MutableLiveData<Integer> getPositionForClip(String clipID) {
        MutableLiveData<Integer> liveData;

        if (positions.containsKey(clipID)) {
            liveData = positions.get(clipID);
        } else {
            liveData = new MutableLiveData<>();
            liveData.setValue(0);
            positions.put(clipID, liveData);
        }

        return liveData;
    }

    public LiveData<Exception> getError() {
        return error;
    }

    private void schedulePositionUpdates() {
        if (!scheduledDurationUpdates) {
            scheduler.schedule(() -> {
                CurrentlyPlaying currentlyPlaying = this.currentlyPlaying.getValue();

                if (currentlyPlaying != null) {
                    MutableLiveData<Integer> position = getPositionForClip(currentlyPlaying.clipID);
                    position.postValue(getMediaPlayer().getCurrentPosition());
                }
            }, 500);
            scheduledDurationUpdates = true;
        }
    }

    private void cancelPositionUpdates() {
        scheduler.cancel();
        scheduledDurationUpdates = false;
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

    private void loadNewClip(String uri) throws IOException {
        getMediaPlayer().reset();
        getMediaPlayer().setDataSource(uri);
        getMediaPlayer().prepare();
    }

    private static class CurrentlyPlaying {

        private final String clipID;
        private final boolean paused;

        CurrentlyPlaying(String clipID, boolean paused) {
            this.clipID = clipID;
            this.paused = paused;
        }

        boolean isPaused() {
            return paused;
        }

        public String getClipID() {
            return clipID;
        }

        CurrentlyPlaying paused() {
            return new CurrentlyPlaying(clipID, true);
        }
    }
}
