package org.odk.collect.android.audio;

import androidx.lifecycle.LiveData;

public class ViewModelAudioPlayer implements AudioPlayer {

    private final AudioPlayerViewModel viewModel;

    public ViewModelAudioPlayer(AudioPlayerViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void play(Clip clip) {
        viewModel.play(clip);
    }

    @Override
    public LiveData<Boolean> isPlaying(String clipId) {
        return viewModel.isPlaying(clipId);
    }

    @Override
    public void pause() {
        viewModel.pause();
    }

    @Override
    public LiveData<Integer> getPosition(String clipId) {
        return viewModel.getPosition(clipId);
    }

    @Override
    public void setPosition(String clipId, Integer position) {
        viewModel.setPosition(clipId, position);
    }
}
