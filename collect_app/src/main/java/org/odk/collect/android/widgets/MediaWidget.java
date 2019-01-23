package org.odk.collect.android.widgets;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.annotation.CallSuper;

import org.javarosa.form.api.FormEntryPrompt;

import timber.log.Timber;

public abstract class MediaWidget extends QuestionWidget {

    private int playColor;

    public MediaWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        String imageURI = this instanceof SelectImageMapWidget ? null : prompt.getImageText();
        String audioURI = prompt.getAudioText();
        String videoURI = prompt.getSpecialFormQuestionText("video");

        // shown when image is clicked
        String bigImageURI = prompt.getSpecialFormQuestionText("big-image");

        getQuestionMediaLayout().setAVT(audioURI, imageURI, videoURI, bigImageURI, player);
        getQuestionMediaLayout().setAudioListener(this);

        initPlayColor();
    }

    private void initPlayColor() {
        playColor = themeUtils.getAccentColor();

        String playColorString = getFormEntryPrompt().getFormElement().getAdditionalAttribute(null, "playColor");
        if (playColorString != null) {
            try {
                playColor = Color.parseColor(playColorString);
            } catch (IllegalArgumentException e) {
                Timber.e(e, "Argument %s is incorrect", playColorString);
            }
        }

        getQuestionMediaLayout().setPlayTextColor(playColor);
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

    public int getPlayColor() {
        return playColor;
    }
}
