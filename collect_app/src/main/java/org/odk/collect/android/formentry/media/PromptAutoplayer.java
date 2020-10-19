package org.odk.collect.android.formentry.media;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.audioclips.Clip;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.odk.collect.android.analytics.AnalyticsEvents.PROMPT;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getClipID;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayableAudioURI;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.NO_BUTTONS;

public class PromptAutoplayer {

    private static final String AUTOPLAY_ATTRIBUTE = "autoplay";
    private static final String AUDIO_OPTION = "audio";

    private final AudioHelper audioHelper;
    private final ReferenceManager referenceManager;
    private final Analytics analytics;
    private final String formIdentifierHash;

    public PromptAutoplayer(AudioHelper audioHelper, ReferenceManager referenceManager, Analytics analytics, String formIdentifierHash) {
        this.audioHelper = audioHelper;
        this.referenceManager = referenceManager;
        this.analytics = analytics;
        this.formIdentifierHash = formIdentifierHash;
    }

    public Boolean autoplayIfNeeded(FormEntryPrompt prompt) {
        String autoplayOption = prompt.getFormElement().getAdditionalAttribute(null, AUTOPLAY_ATTRIBUTE);

        if (hasAudioAutoplay(autoplayOption)) {
            List<Clip> clipsToPlay = new ArrayList<>();

            Clip promptClip = getPromptClip(prompt);
            if (promptClip != null) {
                clipsToPlay.add(promptClip);
                analytics.logEvent(PROMPT, "AutoplayAudioLabel", formIdentifierHash);
            }

            List<Clip> selectClips = getSelectClips(prompt);
            if (!selectClips.isEmpty()) {
                clipsToPlay.addAll(selectClips);
                analytics.logEvent(PROMPT, "AutoplayAudioChoice", formIdentifierHash);
            }

            if (clipsToPlay.isEmpty()) {
                return false;
            } else {
                audioHelper.playInOrder(clipsToPlay);
                return true;
            }
        } else {
            return false;
        }
    }

    private boolean hasAudioAutoplay(String autoplayOption) {
        return autoplayOption != null && autoplayOption.equalsIgnoreCase(AUDIO_OPTION);
    }

    private List<Clip> getSelectClips(FormEntryPrompt prompt) {
        if (appearanceDoesNotShowControls(WidgetAppearanceUtils.getSanitizedAppearanceHint(prompt))) {
            return emptyList();
        }

        List<Clip> selectClips = new ArrayList<>();

        int controlType = prompt.getControlType();
        if (controlType == Constants.CONTROL_SELECT_ONE || controlType == Constants.CONTROL_SELECT_MULTI) {

            List<SelectChoice> selectChoices = prompt.getSelectChoices();

            for (SelectChoice choice : selectChoices) {
                String selectURI = getPlayableAudioURI(prompt, choice, referenceManager);

                if (selectURI != null) {
                    Clip clip = new Clip(getClipID(prompt, choice), selectURI);
                    selectClips.add(clip);
                }
            }
        }

        return selectClips;
    }

    private boolean appearanceDoesNotShowControls(String appearance) {
        return appearance.startsWith(WidgetAppearanceUtils.MINIMAL) ||
                appearance.startsWith(WidgetAppearanceUtils.COMPACT) ||
                appearance.contains(NO_BUTTONS);
    }

    private Clip getPromptClip(FormEntryPrompt prompt) {
        String uri = getPlayableAudioURI(prompt, referenceManager);
        if (uri != null) {
            return new Clip(
                    getClipID(prompt),
                    getPlayableAudioURI(prompt, referenceManager)
            );
        } else {
            return null;
        }
    }
}
