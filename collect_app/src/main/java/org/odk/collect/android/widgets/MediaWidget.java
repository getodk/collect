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

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == INVISIBLE || visibility == GONE) {
            stopAudio();
        }
    }

    public void playVideo() {
        getQuestionMediaLayout().playVideo();
    }

    public void playAudio() {
        playAllPromptText();
    }

    public void stopAudio() {
        if (player != null && player.isPlaying()) {
            player.stop();
            player.reset();
        }
    }

    public MediaPlayer getPlayer() {
        return player;
    }
}
