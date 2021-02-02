package org.odk.collect.android.widgets.utilities;

import android.util.Pair;

import androidx.lifecycle.LifecycleOwner;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;

import java.util.function.Consumer;

public class ViewModelRecordingStatusHandler implements RecordingStatusHandler {

    private final AudioRecorderViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    public ViewModelRecordingStatusHandler(AudioRecorderViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public void onBlockedStatusChange(Consumer<Boolean> blockedStatusListener) {
        viewModel.getCurrentSession().observe(lifecycleOwner, session -> {
            blockedStatusListener.accept(session != null && session.getFile() == null);
        });
    }

    @Override
    public void onRecordingStatusChange(FormEntryPrompt prompt, Consumer<Pair<Long, Integer>> statusListener) {
        viewModel.getCurrentSession().observe(lifecycleOwner, session -> {
            if (session != null && session.getId().equals(prompt.getIndex()) && session.getFailedToStart() == null) {
                statusListener.accept(new Pair<>(session.getDuration(), session.getAmplitude()));
            } else {
                statusListener.accept(null);
            }
        });
    }
}
