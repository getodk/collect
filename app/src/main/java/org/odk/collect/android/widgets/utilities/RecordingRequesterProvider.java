package org.odk.collect.android.widgets.utilities;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.FormEntryPromptUtils;

public class RecordingRequesterProvider {

    private final InternalRecordingRequester internalRecordingRequester;
    private final ExternalAppRecordingRequester externalAppRecordingRequester;

    public RecordingRequesterProvider(InternalRecordingRequester internalRecordingRequester, ExternalAppRecordingRequester externalAppRecordingRequester) {
        this.internalRecordingRequester = internalRecordingRequester;
        this.externalAppRecordingRequester = externalAppRecordingRequester;
    }

    public RecordingRequester create(FormEntryPrompt prompt, boolean externalRecorderPreferred) {
        String audioQuality = FormEntryPromptUtils.getBindAttribute(prompt, "quality");

        if (audioQuality != null && (audioQuality.equals("normal") || audioQuality.equals("voice-only") || audioQuality.equals("low"))) {
            return internalRecordingRequester;
        } else if (audioQuality != null && audioQuality.equals("external")) {
            return externalAppRecordingRequester;
        } else if (externalRecorderPreferred) {
            return externalAppRecordingRequester;
        } else {
            return internalRecordingRequester;
        }
    }
}
