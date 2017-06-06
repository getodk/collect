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

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;

public class DrawView extends View {
    private boolean isSignature;
    private Bitmap mBitmap;
    private Canvas mCanvas;

    private Path mCurrentPath;
    private Path mOffscreenPath; // Adjusted for position of the bitmap in the view

    private Paint mBitmapPaint;
    private Paint paint;
    private Paint pointPaint;

    private File mBackgroundBitmapFile;

    private float mX;
    private float mY;

    private int currentColor = 0xFF000000;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setupView(Context c, boolean isSignature, File f) {
        this.isSignature = isSignature;
        mBackgroundBitmapFile = f;

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mCurrentPath = new Path();
        mOffscreenPath = new Path();
        mBackgroundBitmapFile = new File(Collect.TMPDRAWFILE_PATH);

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

        // Because this activity is used in a fixed landscape mode only, sometimes resetImage()
        // is called upon with flipped w/h (before orientation changes have been applied)
        if (w > h) {
            int temp = w;
            w = h;
            h = temp;
        }

        if (mBackgroundBitmapFile.exists()) {
            mBitmap = FileUtils.getBitmapAccuratelyScaledToDisplay(
                    mBackgroundBitmapFile, w, h).copy(
                    Bitmap.Config.ARGB_8888, true);
            // mBitmap =
            // Bitmap.createScaledBitmap(BitmapFactory.decodeFile(mBackgroundBitmapFile.getPath()),
            // w, h, true);
            mCanvas = new Canvas(mBitmap);
        } else {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mCanvas.drawColor(0xFFFFFFFF);
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
        canvas.drawBitmap(mBitmap, left, top, mBitmapPaint);
        canvas.drawPath(mCurrentPath, paint);
    }

    private void touch_start(float x, float y) {
        mCurrentPath.reset();
        mCurrentPath.moveTo(x, y);

        mOffscreenPath.reset();
        mOffscreenPath.moveTo(x - getBitmapLeft(), y - getBitmapTop());

        mX = x;
        mY = y;
    }

    public void drawSignLine() {
        mCanvas.drawLine(0, (int) (mCanvas.getHeight() * .7),
                mCanvas.getWidth(), (int) (mCanvas.getHeight() * .7), paint);
    }

    private void touch_move(float x, float y) {
        mCurrentPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
        mOffscreenPath.quadTo(mX - getBitmapLeft(), mY - getBitmapTop(),
                (x + mX) / 2 - getBitmapLeft(), (y + mY) / 2 - getBitmapTop());
        mX = x;
        mY = y;
    }

    private void touch_up() {
        if (mCurrentPath.isEmpty()) {
            mCanvas.drawPoint(mX, mY, pointPaint);
        } else {
            mCurrentPath.lineTo(mX, mY);
            mOffscreenPath.lineTo(mX - getBitmapLeft(), mY - getBitmapTop());

            // commit the path to our offscreen
            mCanvas.drawPath(mOffscreenPath, paint);
        }
        // kill this so we don't double draw
        mCurrentPath.reset();
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
        return mBitmap.getHeight();
    }

    public int getBitmapWidth() {
        return mBitmap.getWidth();
    }

    private int getBitmapLeft() {
        // Centered horizontally
        return (getWidth() - mBitmap.getWidth()) / 2;
    }

    private int getBitmapTop() {
        // Centered vertically
        return (getHeight() - mBitmap.getHeight()) / 2;
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