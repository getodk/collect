/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 * @author ctsims
 * @author carlhartung
 */
public class AudioButton extends AppCompatImageButton {

    private AudioHandler handler;
    private MediaPlayer player;
    private Bitmap bitmapPlay;
    private Bitmap bitmapStop;

    public AudioButton(Context context) {
        super(context);
        initView();
    }

    public AudioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        bitmapPlay = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_lock_silent_mode_off);
        bitmapStop = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_pause);

        resetBitmap();
    }

    public void init(FormIndex index, String selectionDesignator, String uri, MediaPlayer player) {
        this.player = player;
        handler = new AudioHandler(index, selectionDesignator, uri, player);
    }

    public void resetBitmap() {
        setImageBitmap(bitmapPlay);
    }

    public void playAudio() {
        handler.playAudio(getContext());
        setImageBitmap(bitmapStop);
    }

    public void onClick() {
        if (player.isPlaying()) {
            player.stop();
            resetBitmap();
        } else {
            playAudio();
        }
    }

    /**
     * Useful class for handling the playing and stopping of audio prompts.
     * This is used here, and also in the GridMultiWidget and GridWidget
     * to play prompts as items are selected.
     *
     * @author mitchellsundt@gmail.com
     */
    public static class AudioHandler {
        private final FormIndex index;
        private final String selectionDesignator;
        private final String uri;
        private final MediaPlayer mediaPlayer;

        public AudioHandler(FormIndex index, String selectionDesignator, String uri,
                            MediaPlayer player) {
            this.index = index;
            this.selectionDesignator = selectionDesignator;
            this.uri = uri;
            mediaPlayer = player;
        }


        public void playAudio(Context c) {
            Collect.getInstance().getActivityLogger().logInstanceAction(this,
                    "onClick.playAudioPrompt", selectionDesignator, index);
            if (uri == null) {
                // No audio file specified
                Timber.e("No audio file was specified");
                ToastUtils.showLongToast(R.string.audio_file_error);
                return;
            }

            String audioFilename = "";
            try {
                audioFilename = ReferenceManager.instance().DeriveReference(uri).getLocalURI();
            } catch (InvalidReferenceException e) {
                Timber.e(e);
            }

            File audioFile = new File(audioFilename);
            if (!audioFile.exists()) {
                // We should have an audio clip, but the file doesn't exist.
                String errorMsg = c.getString(R.string.file_missing, audioFile);
                Timber.e(errorMsg);
                ToastUtils.showLongToast(errorMsg);
                return;
            }

            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(audioFilename);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                String errorMsg = c.getString(R.string.audio_file_invalid, audioFilename);
                Timber.e(errorMsg);
                ToastUtils.showLongToast(errorMsg);
            }
        }
    }
}
