package org.odk.collect.android.widgets.utilities;

import android.util.Pair;

import org.javarosa.form.api.FormEntryPrompt;

import java.util.function.Consumer;

public interface RecordingStatusHandler {

    void onBlockedStatusChange(Consumer<Boolean> blockedStatusListener);

    void onRecordingStatusChange(FormEntryPrompt prompt, Consumer<Pair<Long, Integer>> statusListener);
}
