package org.odk.collect.android.widgets.utilities;

import org.javarosa.form.api.FormEntryPrompt;

public interface AudioDataRequester {
    void requestRecording(FormEntryPrompt prompt);

    void requestFile(FormEntryPrompt prompt);
}
