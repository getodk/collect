package org.odk.collect.android.views.helpers;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;

import timber.log.Timber;

public final class FormMediaHelpers {

    private FormMediaHelpers() {

    }

    public static String getClipID(FormEntryPrompt formEntryPrompt) {
        return formEntryPrompt.getIndex().toString();
    }

    public static String getPlayableAudioURI(String audioURI, ReferenceManager referenceManager) {
        String uri = null;
        try {
            uri = referenceManager.deriveReference(audioURI).getLocalURI();
        } catch (InvalidReferenceException e) {
            Timber.e(e);
        }

        return uri;
    }
}
