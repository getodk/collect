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
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

/**
 * @author ctsims
 * @author carlhartung
 */
public class AudioButton extends AppCompatImageButton implements View.OnClickListener {

    private Bitmap bitmapPlay;
    private Bitmap bitmapStop;
    private Listener listener;

    private Boolean playing = false;

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

    public Boolean isPlaying() {
        return playing;
    }

    public void setPlaying(Boolean isPlaying) {
        playing = isPlaying;

        if (isPlaying) {
            setImageBitmap(bitmapStop);
        } else {
            setImageBitmap(bitmapPlay);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void resetBitmap() {
        setImageBitmap(bitmapPlay);
    }

    @Override
    public void onClick(View view) {
        if (playing) {
            listener.onStopClicked();
        } else {
            listener.onPlayClicked();
        }
    }

    public interface Listener {

        void onPlayClicked();

        void onStopClicked();
    }
}
