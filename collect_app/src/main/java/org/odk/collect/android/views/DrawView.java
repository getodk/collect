/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.utilities.ImageFileUtils;

import java.io.File;

public class DrawView extends View {
    private boolean isSignature;
    private Bitmap bitmap;
    private Canvas canvas;

    private Path currentPath;
    private Path offscreenPath; // Adjusted for position of the bitmap in the view

    private Paint bitmapPaint;
    private Paint paint;
    private Paint pointPaint;

    private File backgroundBitmapFile;

    private float valueX;
    private float valueY;

    private int currentColor = 0xFF000000;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setupView(boolean isSignature) {
        this.isSignature = isSignature;

        bitmapPaint = new Paint(Paint.DITHER_FLAG);
        currentPath = new Path();
        offscreenPath = new Path();
        backgroundBitmapFile = new File(new StoragePathProvider().getTmpImageFilePath());

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(currentColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(10);

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setDither(true);
        pointPaint.setColor(currentColor);
        pointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pointPaint.setStrokeWidth(10);
    }

    public void reset() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        resetImage(screenWidth, screenHeight);
    }

    public void resetImage(int w, int h) {
        if (backgroundBitmapFile.exists()) {
            bitmap = ImageFileUtils.getBitmapScaledToDisplay(backgroundBitmapFile, h, w, true)
                    .copy(Bitmap.Config.ARGB_8888, true);
            canvas = new Canvas(bitmap);
        } else {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            canvas.drawColor(0xFFFFFFFF);
            if (isSignature) {
                drawSignLine();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetImage(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawOnCanvas(canvas, getBitmapLeft(), getBitmapTop());
    }

    public void drawOnCanvas(Canvas canvas, float left, float top) {
        canvas.drawColor(0xFFAAAAAA);
        canvas.drawBitmap(bitmap, left, top, bitmapPaint);
        canvas.drawPath(currentPath, paint);
    }

    private void touch_start(float x, float y) {
        currentPath.reset();
        currentPath.moveTo(x, y);

        offscreenPath.reset();
        offscreenPath.moveTo(x - getBitmapLeft(), y - getBitmapTop());

        valueX = x;
        valueY = y;
    }

    public void drawSignLine() {
        canvas.drawLine(0, (int) (canvas.getHeight() * .7),
                canvas.getWidth(), (int) (canvas.getHeight() * .7), paint);
    }

    private void touch_move(float x, float y) {
        currentPath.quadTo(valueX, valueY, (x + valueX) / 2, (y + valueY) / 2);
        offscreenPath.quadTo(valueX - getBitmapLeft(), valueY - getBitmapTop(),
                (x + valueX) / 2 - getBitmapLeft(), (y + valueY) / 2 - getBitmapTop());
        valueX = x;
        valueY = y;
    }

    private void touch_up() {
        if (currentPath.isEmpty()) {
            canvas.drawPoint(valueX, valueY, pointPaint);
        } else {
            currentPath.lineTo(valueX, valueY);
            offscreenPath.lineTo(valueX - getBitmapLeft(), valueY - getBitmapTop());

            // commit the path to our offscreen
            canvas.drawPath(offscreenPath, paint);
        }
        // kill this so we don't double draw
        currentPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public int getBitmapHeight() {
        return bitmap.getHeight();
    }

    public int getBitmapWidth() {
        return bitmap.getWidth();
    }

    private int getBitmapLeft() {
        // Centered horizontally
        return (getWidth() - bitmap.getWidth()) / 2;
    }

    private int getBitmapTop() {
        // Centered vertically
        return (getHeight() - bitmap.getHeight()) / 2;
    }

    public void setColor(int color) {
        currentColor = color;
        paint.setColor(color);
        pointPaint.setColor(color);
    }

    public int getColor() {
        return currentColor;
    }
}