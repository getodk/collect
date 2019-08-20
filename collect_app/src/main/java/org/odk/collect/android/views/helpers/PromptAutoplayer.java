package org.odk.collect.android.views.helpers;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.audio.Clip;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collect.android.views.helpers.FormMediaHelpers.getClipID;
import static org.odk.collect.android.views.helpers.FormMediaHelpers.getPlayableAudioURI;

public class PromptAutoplayer {

    private static final String AUTOPLAY_ATTRIBUTE = "autoplay";
    private static final String AUDIO_OPTION = "audio";

    private final AudioHelper audioHelper;
    private final ReferenceManager referenceManager;

    public PromptAutoplayer(AudioHelper audioHelper, ReferenceManager referenceManager) {
        this.audioHelper = audioHelper;
        this.referenceManager = referenceManager;
    }

    public Boolean autoplayIfNeeded(FormEntryPrompt prompt) {
        String autoplayOption = prompt.getFormElement().getAdditionalAttribute(null, AUTOPLAY_ATTRIBUTE);

        if (autoplayOption != null && autoplayOption.equalsIgnoreCase(AUDIO_OPTION)) {
            String uri = getPlayableAudioURI(prompt, referenceManager);
            List<Clip> clips = new ArrayList<>();

            if (uri != null) {
                clips.add(new Clip(getClipID(prompt), getPlayableAudioURI(prompt, referenceManager)));
            }

            if (prompt.getControlType() == Constants.CONTROL_SELECT_ONE) {
                List<SelectChoice> selectChoices = prompt.getSelectChoices();

                for (SelectChoice choice : selectChoices) {
                    String selectURI = getPlayableAudioURI(prompt, choice, referenceManager);

                    if (selectURI != null) {
                        clips.add(new Clip(getClipID(prompt, choice), selectURI));
                    }
                }
            }

            if (clips.isEmpty()) {
                return false;
            } else if (clips.size() == 1) {
                audioHelper.play(clips.get(0).getClipID(), clips.get(0).getURI());
                return true;
            } else {
                audioHelper.playInOrder(clips);
                return true;
            }

        } else {
            return false;
        }
    }
}
