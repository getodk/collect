package org.odk.collect.android.formentry.media;

import android.graphics.Color;

import androidx.annotation.Nullable;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.audioclips.Clip;

import timber.log.Timber;

public final class FormMediaUtils {

    private FormMediaUtils() {

    }

    @Nullable
    public static Clip getClip(FormEntryPrompt prompt, SelectChoice selectChoice, ReferenceManager referenceManager) {
        String playableAudioURI = getPlayableAudioURI(prompt, selectChoice, referenceManager);

        if (playableAudioURI != null) {
            return new Clip(getClipID(prompt, selectChoice), playableAudioURI);
        } else {
            return null;
        }
    }

    public static String getClipID(FormEntryPrompt prompt) {
        return prompt.getIndex().toString();
    }

    public static String getClipID(FormEntryPrompt prompt, SelectChoice selectChoice) {
        return prompt.getIndex().toString() + " " + selectChoice.getIndex();
    }

    @Nullable
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

    public static int getPlayColor(FormEntryPrompt prompt, ThemeUtils themeUtils) {
        int playColor = themeUtils.getAccentColor();

        String playColorString = prompt.getFormElement().getAdditionalAttribute(null, "playColor");
        if (playColorString != null) {
            try {
                playColor = Color.parseColor(playColorString);
            } catch (IllegalArgumentException e) {
                Timber.e(e, "Argument %s is incorrect", playColorString);
            }
        }

        return playColor;
    }
}
