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
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

/**
 * @author ctsims
 * @author carlhartung
 */
public class AudioButton extends AppCompatImageButton {

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

    public void resetBitmap() {
        setImageBitmap(bitmapPlay);
    }

    public void playAudio() {
        setImageBitmap(bitmapStop);
    }
}
