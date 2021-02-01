package org.odk.collect.android.widgets.utilities;

import android.util.Pair;

import androidx.lifecycle.LifecycleOwner;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.audiorecorder.recording.AudioRecorder;

import java.util.function.Consumer;

public class AudioRecorderRecordingStatusHandler implements RecordingStatusHandler {

    private final AudioRecorder audioRecorder;
    private final LifecycleOwner lifecycleOwner;

    public AudioRecorderRecordingStatusHandler(AudioRecorder audioRecorder, LifecycleOwner lifecycleOwner) {
        this.audioRecorder = audioRecorder;
        this.lifecycleOwner = lifecycleOwner;
    }

    @Override
    public void onBlockedStatusChange(Consumer<Boolean> blockedStatusListener) {
        audioRecorder.getCurrentSession().observe(lifecycleOwner, session -> {
            blockedStatusListener.accept(session != null && session.getFile() == null);
        });
    }

    @Override
    public void onRecordingStatusChange(FormEntryPrompt prompt, Consumer<Pair<Long, Integer>> statusListener) {
        audioRecorder.getCurrentSession().observe(lifecycleOwner, session -> {
            if (session != null && session.getId().equals(prompt.getIndex()) && session.getFailedToStart() == null) {
                statusListener.accept(new Pair<>(session.getDuration(), session.getAmplitude()));
            } else {
                statusListener.accept(null);
            }
        });
    }
}
