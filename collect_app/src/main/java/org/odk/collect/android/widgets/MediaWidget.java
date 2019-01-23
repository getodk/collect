package org.odk.collect.android.widgets;

import android.content.Context;
import android.media.MediaPlayer;

import org.javarosa.form.api.FormEntryPrompt;

public abstract class MediaWidget extends QuestionWidget {

    public MediaWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    @Override
    public void release() {
        super.release();

        if (player != null) {
            player.release();
            player = null;
        }
    }

    public MediaPlayer getPlayer() {
        return player;
    }
}
