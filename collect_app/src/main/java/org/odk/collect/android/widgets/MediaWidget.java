package org.odk.collect.android.widgets;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.CallSuper;

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

    /*
     * Prompts with items must override this
     */
    @CallSuper
    public void playAllPromptText(String playOption) {
        if (playOption.equalsIgnoreCase("audio")) {
            getQuestionMediaLayout().playAudio();
        } else if (playOption.equalsIgnoreCase("video")) {
            getQuestionMediaLayout().playVideo();
        }
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
