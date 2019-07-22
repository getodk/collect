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

package org.odk.collect.android.audio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 * @author ctsims
 * @author carlhartung
 */
public class AudioButton extends AppCompatImageButton implements View.OnClickListener {

    private Bitmap bitmapPlay;
    private Bitmap bitmapStop;
    private Boolean playing;
    private OnPlayStopListener onPlayStopListener;

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
        this.setOnClickListener(this);
    }

    public void setPlaying(Boolean isPlaying) {
        playing = isPlaying;

        if (isPlaying) {
            setImageBitmap(bitmapStop);
        } else {
            setImageBitmap(bitmapPlay);
        }
    }

    public void setOnPlayStopListener(OnPlayStopListener onPlayStopListener) {
        this.onPlayStopListener = onPlayStopListener;
    }

    public void resetBitmap() {
        setImageBitmap(bitmapPlay);
    }

    public void onClick() {

    }

    @Override
    public void onClick(View view) {
        if (playing) {
            onPlayStopListener.onStop();
        } else {
            onPlayStopListener.onPlay();
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

    public interface OnPlayStopListener {

        void onPlay();

        void onStop();
    }
}
