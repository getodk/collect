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
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

import org.odk.collect.android.R;

/**
 * @author ctsims
 * @author carlhartung
 */
public class AudioButton extends AppCompatImageButton implements View.OnClickListener {

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
        reset();
        this.setOnClickListener(this);
    }

    public Boolean isPlaying() {
        return playing;
    }

    public void setPlaying(Boolean isPlaying) {
        playing = isPlaying;

        if (isPlaying) {
            setImageResource(R.drawable.ic_stop_24dp);
        } else {
            setImageResource(R.drawable.ic_volume_up_24dp);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void reset() {
        setImageResource(R.drawable.ic_volume_up_24dp);
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
