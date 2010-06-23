/*
 * Copyright (C) 2009 University of Washington
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

import org.odk.collect.android.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Builds view for arrow animation
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class ArrowAnimationView extends View {

    private final static String t = "ArrowAnimationView";

    private Animation mAnimation;
    private Bitmap mArrow;

    private int mImgXOffset;


    public ArrowAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(t, "called constructor");
        init();
    }


    public ArrowAnimationView(Context context) {
        super(context);
        init();
    }


    private void init() {
        mArrow = BitmapFactory.decodeResource(getResources(), R.drawable.left_arrow);
        mImgXOffset = mArrow.getWidth() / 2;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mAnimation == null) {
            createAnim(canvas);
        }

        int centerX = canvas.getWidth() / 2;

        canvas.drawBitmap(mArrow, centerX - mImgXOffset, (float) mArrow.getHeight() / 4, null);
    }


    private void createAnim(Canvas canvas) {
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.start_arrow);
        startAnimation(mAnimation);
    }
}
