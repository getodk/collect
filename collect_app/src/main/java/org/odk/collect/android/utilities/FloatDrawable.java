/*
 * Copyright 2018 Yizheng Huang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.utilities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class FloatDrawable extends Drawable {

    private Context context;
    private int offset = 50;
    private Paint linePaint = new Paint();
    private Paint linePaint2 = new Paint();

    public FloatDrawable(Context context) {
        super();
        this.context = context;

        linePaint.setARGB(200, 50, 50, 50);
        linePaint.setStrokeWidth(1F);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.WHITE);

        linePaint2.setARGB(200, 50, 50, 50);
        linePaint2.setStrokeWidth(7F);
        linePaint2.setStyle(Paint.Style.STROKE);
        linePaint2.setAntiAlias(true);
        linePaint2.setColor(Color.WHITE);

    }

    // adaption problem addressed here.
    public int getBorderWidth() {
        return dipToPx(context, offset);
    }

    public int getBorderHeight() {
        return dipToPx(context, offset);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        int left = getBounds().left;
        int top = getBounds().top;
        int right = getBounds().right;
        int bottom = getBounds().bottom;

        Rect rect = new Rect(left + dipToPx(context, offset) / 2,
                top + dipToPx(context, offset) / 2,
                right - dipToPx(context, offset) / 2,
                bottom - dipToPx(context, offset) / 2);
        //Default Rect
        canvas.drawRect(rect, linePaint);
        //Lines in Rect
        canvas.drawLine((left + dipToPx(context, offset) / 2 - 3.5f),
                top + dipToPx(context, offset) / 2,
                left + dipToPx(context, offset) - 8f,
                top + dipToPx(context, offset) / 2, linePaint2);
        canvas.drawLine(left + dipToPx(context, offset) / 2,
                top + dipToPx(context, offset) / 2,
                left + dipToPx(context, offset) / 2,
                top + dipToPx(context, offset) / 2 + 30, linePaint2);
        canvas.drawLine(right - dipToPx(context, offset) + 8f,
                top + dipToPx(context, offset) / 2,
                right - dipToPx(context, offset) / 2,
                top + dipToPx(context, offset) / 2, linePaint2);
        canvas.drawLine(right - dipToPx(context, offset) / 2,
                top + dipToPx(context, offset) / 2 - 3.5f,
                right - dipToPx(context, offset) / 2,
                top + dipToPx(context, offset) / 2 + 30, linePaint2);
        canvas.drawLine((left + dipToPx(context, offset) / 2 - 3.5f),
                bottom - dipToPx(context, offset) / 2,
                left + dipToPx(context, offset) - 8f,
                bottom - dipToPx(context, offset) / 2, linePaint2);
        canvas.drawLine((left + dipToPx(context, offset) / 2),
                bottom - dipToPx(context, offset) / 2,
                left + dipToPx(context, offset) / 2,
                bottom - dipToPx(context, offset) / 2 - 30f, linePaint2);
        canvas.drawLine((right - dipToPx(context, offset) + 8f),
                bottom - dipToPx(context, offset) / 2,
                right - dipToPx(context, offset) / 2,
                bottom - dipToPx(context, offset) / 2, linePaint2);
        canvas.drawLine((right - dipToPx(context, offset) / 2),
                bottom - dipToPx(context, offset) / 2 - 30f,
                right - dipToPx(context, offset) / 2,
                bottom - dipToPx(context, offset) / 2 + 3.5f, linePaint2);
    }

    @Override
    public void setBounds(@NonNull Rect bounds) {
        super.setBounds(new Rect(bounds.left - dipToPx(context, offset) / 2,
                bounds.top - dipToPx(context, offset) / 2,
                bounds.right + dipToPx(context, offset) / 2,
                bounds.bottom + dipToPx(context, offset) / 2));
    }

    @Override
    public void setAlpha(int alpha) {
        // setAlpha value
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // set color filter
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    public int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}