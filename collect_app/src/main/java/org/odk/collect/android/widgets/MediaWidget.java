package org.odk.collect.android.widgets;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.annotation.CallSuper;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.AudioPlayListener;

import timber.log.Timber;

public abstract class MediaWidget extends QuestionWidget implements AudioPlayListener {

    private int playColor;
    private MediaPlayer player;

    public MediaWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        initMediaPlayer();
        attachMediaToLayout();
        initPlayColor();
    }

    private void initMediaPlayer() {
        player = new MediaPlayer();
        player.setOnCompletionListener(mediaPlayer -> {
            getQuestionMediaLayout().resetTextFormatting();
            mediaPlayer.reset();
        });
        player.setOnErrorListener((mp, what, extra) -> {
            Timber.e("Error occurred in MediaPlayer. what = %d, extra = %d", what, extra);
            return false;
        });
    }

    private void attachMediaToLayout() {
        String imageURI = this instanceof SelectImageMapWidget ? null : getFormEntryPrompt().getImageText();
        String audioURI = getFormEntryPrompt().getAudioText();
        String videoURI = getFormEntryPrompt().getSpecialFormQuestionText("video");

        // shown when image is clicked
        String bigImageURI = getFormEntryPrompt().getSpecialFormQuestionText("big-image");

        getQuestionMediaLayout().setAVT(audioURI, imageURI, videoURI, bigImageURI, player);
        getQuestionMediaLayout().setAudioListener(this);
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
    public void resetQuestionTextColor() {
        getQuestionMediaLayout().resetTextFormatting();
    }

    @Override
    public void resetAudioButtonImage() {
        getQuestionMediaLayout().resetAudioButtonBitmap();
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
