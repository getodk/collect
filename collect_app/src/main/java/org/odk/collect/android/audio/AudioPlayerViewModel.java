package org.odk.collect.android.audio;

import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.Scheduler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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

    public void play(Clip clip) {
        LinkedList<Clip> playlist = new LinkedList<>();
        playlist.add(clip);
        playNext(playlist);
    }

    public void playInOrder(List<Clip> clips) {
        Queue<Clip> playlist = new LinkedList<>(clips);
        playNext(playlist);
    }

    public void stop() {
        if (currentlyPlaying.getValue() != null) {
            getMediaPlayer().stop();
        }

        cleanUpAfterClip();
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

    public void background() {
        cleanUpAfterClip();
        releaseMediaPlayer();
    }

    @Override
    protected void onCleared() {
        background();
    }

    private void playNext(Queue<Clip> playlist) {
        Clip nextClip = playlist.poll();

        if (nextClip != null) {

            if (!isCurrentPlayingClip(nextClip.getClipID(), currentlyPlaying.getValue())) {
                try {
                    loadNewClip(nextClip.getURI());
                } catch (IOException ignored) {
                    error.setValue(new PlaybackFailedException(nextClip.getURI(), getExceptionMsg(nextClip.getURI())));
                    playNext(playlist);
                    return;
                }
            }

            getMediaPlayer().seekTo(getPositionForClip(nextClip.getClipID()).getValue());
            getMediaPlayer().start();

            currentlyPlaying.setValue(new CurrentlyPlaying(
                    new Clip(nextClip.getClipID(), nextClip.getURI()),
                    false,
                    playlist));

            schedulePositionUpdates();
        }
    }

    private int getExceptionMsg(String uri) {
        return new File(uri).exists() ? R.string.file_invalid : R.string.file_missing;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        CurrentlyPlaying wasPlaying = cleanUpAfterClip();

        if (wasPlaying != null) {
            if (!wasPlaying.getPlaylist().isEmpty()) {
                playNext(wasPlaying.getPlaylist());
            }
        }
    }

    private CurrentlyPlaying cleanUpAfterClip() {
        CurrentlyPlaying wasPlaying = currentlyPlaying.getValue();
        cancelPositionUpdates();
        currentlyPlaying.setValue(null);

        if (wasPlaying != null) {
            getPositionForClip(wasPlaying.getClipID()).setValue(0);
        }

        return wasPlaying;
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

    public void dismissError() {
        error.setValue(null);
    }

    private void schedulePositionUpdates() {
        if (!scheduledDurationUpdates) {
            scheduler.schedule(() -> {
                CurrentlyPlaying currentlyPlaying = this.currentlyPlaying.getValue();

                if (currentlyPlaying != null) {
                    MutableLiveData<Integer> position = getPositionForClip(currentlyPlaying.clip.getClipID());
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
        return currentlyPlayingValue != null && currentlyPlayingValue.clip.getClipID().equals(clipID);
    }

    private void loadNewClip(String uri) throws IOException {
        getMediaPlayer().reset();
        getMediaPlayer().setDataSource(uri);
        getMediaPlayer().prepare();
    }

    private static class CurrentlyPlaying {

        private final Clip clip;
        private final boolean paused;
        private final Queue<Clip> playlist;

        CurrentlyPlaying(Clip clip, boolean paused, Queue<Clip> playlist) {
            this.clip = clip;
            this.paused = paused;
            this.playlist = playlist;
        }

        boolean isPaused() {
            return paused;
        }

        String getClipID() {
            return clip.getClipID();
        }

        CurrentlyPlaying paused() {
            return new CurrentlyPlaying(clip, true, playlist);
        }

        Queue<Clip> getPlaylist() {
            return playlist;
        }

        Clip getClip() {
            return clip;
        }
    }
}
