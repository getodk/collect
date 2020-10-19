package org.odk.collect.android.widgets.utilities;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import org.odk.collect.audioclips.AudioClipViewModel;
import org.odk.collect.audioclips.Clip;

import java.util.function.Consumer;

public class ViewModelAudioPlayer implements AudioPlayer {

    private final AudioClipViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    public ViewModelAudioPlayer(AudioClipViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public void play(Clip clip) {
        viewModel.play(clip);
    }

    @Override
    public void pause() {
        viewModel.pause();
    }

    @Override
    public void setPosition(String clipId, Integer position) {
        viewModel.setPosition(clipId, position);
    }

    @Override
    public void onPlayingChanged(String clipID, Consumer<Boolean> playingConsumer) {
        viewModel.isPlaying(clipID).observe(lifecycleOwner, (Observer<Boolean>) playingConsumer::accept);
    }

    @Override
    public void onPositionChanged(String clipID, Consumer<Integer> positionConsumer) {
        viewModel.getPosition(clipID).observe(lifecycleOwner, (Observer<Integer>) positionConsumer::accept);
    }

    @Override
    public void stop() {
        viewModel.stop();
    }
}
