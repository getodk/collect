package org.odk.collect.android.utilities;

import android.content.Context;
import android.media.MediaPlayer;

import org.odk.collect.android.R;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 * Useful class for handling the playing and stopping of audio prompts.
 * This is used here, and also in the GridMultiWidget and GridWidget
 * to play prompts as items are selected.
 *
 * @author mitchellsundt@gmail.com
 */
public class AudioHandler {
    private final String uri;
    private final MediaPlayer mediaPlayer;

    public AudioHandler(String uri, MediaPlayer mediaPlayer) {
        this.uri = uri;
        this.mediaPlayer = mediaPlayer;
    }

    public void playAudio(Context c) {
        if (uri == null) {
            // No audio file specified
            Timber.e("No audio file was specified");
            ToastUtils.showLongToast(R.string.audio_file_error);
            return;
        }

        File audioFile = new File(uri);
        if (!audioFile.exists()) {
            // We should have an audio clip, but the file doesn't exist.
            String errorMsg = c.getString(R.string.file_missing, audioFile);
            Timber.e(errorMsg);
            ToastUtils.showLongToast(errorMsg);
            return;
        }

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            String errorMsg = c.getString(R.string.audio_file_invalid, uri);
            Timber.e(errorMsg);
            ToastUtils.showLongToast(errorMsg);
        }
    }
}
