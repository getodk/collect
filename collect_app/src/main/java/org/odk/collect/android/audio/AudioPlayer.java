package org.odk.collect.android.audio;

import androidx.lifecycle.LiveData;

public interface AudioPlayer {

    void play(Clip clip);

    LiveData<Boolean> isPlaying(String clipId);

    void pause();

    LiveData<Integer> getPosition(String clipId);

    void setPosition(String clipId, Integer position);
}
