package org.odk.collect.android.views.helpers;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.audio.AudioHelper;

import static org.odk.collect.android.views.helpers.FormMediaHelpers.getClipID;
import static org.odk.collect.android.views.helpers.FormMediaHelpers.getPlayableAudioURI;

public class FormAutoplayHelper {

    private static final String AUTOPLAY_ATTRIBUTE = "autoplay";
    private static final String AUDIO_OPTION = "audio";

    private final AudioHelper audioHelper;
    private final ReferenceManager referenceManager;

    public FormAutoplayHelper(AudioHelper audioHelper, ReferenceManager referenceManager) {
        this.audioHelper = audioHelper;
        this.referenceManager = referenceManager;
    }

    public Boolean autoplayIfNeeded(FormEntryPrompt prompt) {
        String autoplayOption = prompt.getFormElement().getAdditionalAttribute(null, AUTOPLAY_ATTRIBUTE);

        if (autoplayOption != null && autoplayOption.equalsIgnoreCase(AUDIO_OPTION)) {
            String uri = prompt.getAudioText();

            if (uri != null) {
                audioHelper.play(getClipID(prompt), getPlayableAudioURI(uri, referenceManager));
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
