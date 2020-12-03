package org.odk.collect.android.widgets.utilities;

import android.util.Pair;

import org.javarosa.form.api.FormEntryPrompt;

import java.util.function.Consumer;

public interface RecordingRequester {
    void requestRecording(FormEntryPrompt prompt);

    void onIsRecordingChanged(Consumer<Boolean> isRecordingListener);

    void onRecordingInProgress(FormEntryPrompt prompt, Consumer<Pair<Long, Integer>> durationListener);

    void onRecordingAvailable(FormEntryPrompt prompt, Consumer<String> recordingAvailableListener);
}
