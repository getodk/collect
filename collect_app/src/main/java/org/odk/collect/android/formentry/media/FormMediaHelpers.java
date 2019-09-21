package org.odk.collect.android.formentry.media;

import androidx.annotation.Nullable;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;

import timber.log.Timber;

public final class FormMediaHelpers {

    private FormMediaHelpers() {

    }

    public static String getClipID(FormEntryPrompt prompt) {
        return prompt.getIndex().toString();
    }

    public static String getClipID(FormEntryPrompt prompt, SelectChoice selectChoice) {
        return prompt.getIndex().toString() + " " + selectChoice.getIndex();
    }

    public static String getPlayableAudioURI(FormEntryPrompt prompt, ReferenceManager referenceManager) {
        return deriveReference(prompt.getAudioText(), referenceManager);
    }

    @Nullable
    public static String getPlayableAudioURI(FormEntryPrompt prompt, SelectChoice selectChoice, ReferenceManager referenceManager) {
        String selectAudioURI = prompt.getSpecialFormSelectChoiceText(
                selectChoice,
                FormEntryCaption.TEXT_FORM_AUDIO
        );

        return deriveReference(selectAudioURI, referenceManager);
    }

    @Nullable
    private static String deriveReference(String originalURI, ReferenceManager referenceManager) {
        if (originalURI == null) {
            return null;
        }

        try {
            return referenceManager.deriveReference(originalURI).getLocalURI();
        } catch (InvalidReferenceException e) {
            Timber.e(e);
            return null;
        }
    }
}
