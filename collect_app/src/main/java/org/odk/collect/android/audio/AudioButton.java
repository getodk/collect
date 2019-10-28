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
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.button.MaterialButton;

import org.odk.collect.android.R;

/**
 * @author ctsims
 * @author carlhartung
 */
public class AudioButton extends MaterialButton implements View.OnClickListener {

    private Listener listener;

    private Boolean playing = false;
    private Integer playingColor;
    private Integer idleColor;

    public AudioButton(Context context) {
        super(context, null);
        initView();
    }

    public AudioButton(Context context, AttributeSet attrs) {
        super(context, attrs, com.google.android.material.R.style.Widget_MaterialComponents_Button_OutlinedButton_Icon);
        initView();
    }

    public Boolean isPlaying() {
        return playing;
    }

    public void setColors(Integer idleColor, Integer playingColor) {
        this.idleColor = idleColor;
        this.playingColor = playingColor;
        render();
    }

    public void setPlaying(Boolean isPlaying) {
        playing = isPlaying;
        render();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (playing) {
            listener.onStopClicked();
        } else {
            listener.onPlayClicked();
        }
    }

    private void initView() {
        this.setOnClickListener(this);
        render();
    }

    private void render() {
        if (playing) {
            setIconResource(R.drawable.ic_stop_black_24dp);

            if (playingColor != null) {
                setIconTint(ColorStateList.valueOf(playingColor));
            }
        } else {
            setIconResource(R.drawable.ic_volume_up_black_24dp);

            if (idleColor != null) {
                setIconTint(ColorStateList.valueOf(idleColor));
            }
        }
    }

    public interface Listener {

        void onPlayClicked();

        void onStopClicked();
    }
}
