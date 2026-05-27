package org.odk.collect.android.widgets.utilities;

import static org.odk.collect.android.widgets.utilities.BindAttributes.QUALITY;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.widgets.utilities.BindAttributes.Quality;

public class RecordingRequesterProvider {

    private final InternalRecordingRequester internalRecordingRequester;
    private final ExternalAppRecordingRequester externalAppRecordingRequester;

    public RecordingRequesterProvider(InternalRecordingRequester internalRecordingRequester, ExternalAppRecordingRequester externalAppRecordingRequester) {
        this.internalRecordingRequester = internalRecordingRequester;
        this.externalAppRecordingRequester = externalAppRecordingRequester;
    }

    public RecordingRequester create(FormEntryPrompt prompt, boolean externalRecorderPreferred) {
        String audioQuality = FormEntryPromptUtils.getBindAttribute(prompt, QUALITY);

        if (audioQuality != null && (audioQuality.equals(Quality.NORMAL.getValue()) || audioQuality.equals(Quality.VOICE_ONLY.getValue()) || audioQuality.equals(Quality.LOW.getValue()))) {
            return internalRecordingRequester;
        } else if (audioQuality != null && audioQuality.equals(Quality.EXTERNAL.getValue())) {
            return externalAppRecordingRequester;
        } else if (externalRecorderPreferred) {
            return externalAppRecordingRequester;
        } else {
            return internalRecordingRequester;
        }
    }
}
