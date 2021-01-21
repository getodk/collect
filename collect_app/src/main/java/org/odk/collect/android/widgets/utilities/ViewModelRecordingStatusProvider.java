package org.odk.collect.android.widgets.utilities;

import android.util.Pair;

import androidx.lifecycle.LifecycleOwner;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.audiorecorder.recording.AudioRecorderViewModel;

import java.util.function.Consumer;

public class ViewModelRecordingStatusProvider implements RecordingStatusProvider {

    private final AudioRecorderViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    public ViewModelRecordingStatusProvider(AudioRecorderViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public void onIsRecordingBlocked(Consumer<Boolean> isRecordingBlockedListener) {
        viewModel.getCurrentSession().observe(lifecycleOwner, session -> {
            isRecordingBlockedListener.accept(session != null && session.getFile() == null);
        });
    }

    @Override
    public void onRecordingInProgress(FormEntryPrompt prompt, Consumer<Pair<Long, Integer>> durationListener) {
        viewModel.getCurrentSession().observe(lifecycleOwner, session -> {
            if (session != null && session.getId().equals(prompt.getIndex()) && session.getFailedToStart() == null) {
                durationListener.accept(new Pair<>(session.getDuration(), session.getAmplitude()));
            } else {
                durationListener.accept(null);
            }
        });
    }
}
