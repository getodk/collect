package org.odk.collect.android.widgets.utilities;

import org.javarosa.form.api.FormEntryPrompt;

import java.util.function.Consumer;

public interface RecordingRequester {
    void requestRecording(FormEntryPrompt prompt);

    void onIsRecordingChanged(Consumer<Boolean> isRecordingListener);

    void onRecordingAvailable(FormEntryPrompt prompt, Consumer<String> recordingAvailableListener);
}
