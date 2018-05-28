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

package org.odk.collect.android.views;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.odk.collect.android.utilities.FloatDrawable;

import timber.log.Timber;

public class CropImageView extends View {
    // Touch point
    private float pointX = 0;
    private float pointY = 0;
    // Touch events
    private static final int STATUS_SINGLE = 1;
    private static final int STATUS_MULTI_START = 2;
    private static final int STATUS_MULTI_TOUCHING = 3;
    // Current status
    private int status = STATUS_SINGLE;
    // Default height & width
    private int cropWidth;
    private int cropHeight;
    // Four points of the float layer
    private static final int EDGE_LT = 1;
    private static final int EDGE_RT = 2;
    private static final int EDGE_LB = 3;
    private static final int EDGE_RB = 4;
    private static final int EDGE_MOVE_IN = 5;
    private static final int EDGE_MOVE_OUT = 6;
    private static final int EDGE_NONE = 7;

    public int currentEdge = EDGE_NONE;

    protected float oriRationWH = 0;

    protected Drawable drawable;
    protected FloatDrawable floatDrawable;

    protected Rect drawableSrc = new Rect();
    protected Rect drawableDst = new Rect();
    protected Rect drawableFloat = new Rect();
    protected boolean isFirst = true;
    private boolean isTouchInSquare = true;

    protected Context context;
    private Bitmap bitmap;

    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressLint("NewApi")
    private void init(Context context) {
        this.context = context;
        try {
            // use a software way to draw views.
            this.setLayerType(LAYER_TYPE_SOFTWARE, null);
        } catch (Exception e) {
            Timber.e(e.toString());
        }
        floatDrawable = new FloatDrawable(context);
    }

    public void setDrawable(Bitmap bitmap, int cropWidth, int cropHeight) {
        this.bitmap = bitmap;
        this.drawable = new BitmapDrawable(bitmap);
        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;
        this.isFirst = true;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (bitmap != null) {
            if ((bitmap.getHeight() > heightSize) && (bitmap.getHeight() > bitmap.getWidth())) {
                widthSize = heightSize * bitmap.getWidth() / bitmap.getHeight();
            } else if ((bitmap.getWidth() > widthSize) && (bitmap.getWidth() > bitmap.getHeight())) {
                heightSize = widthSize * bitmap.getHeight() / bitmap.getWidth();
            } else {
                heightSize = bitmap.getHeight();
                widthSize = bitmap.getWidth();
            }
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            if (status == STATUS_SINGLE) {
                status = STATUS_MULTI_START;
            } else if (status == STATUS_MULTI_START) {
                status = STATUS_MULTI_TOUCHING;
            }
        } else {
            if (status == STATUS_MULTI_START
                    || status == STATUS_MULTI_TOUCHING) {
                pointX = event.getX();
                pointY = event.getY();
            }
            status = STATUS_SINGLE;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointX = event.getX();
                pointY = event.getY();
                currentEdge = getTouch((int) pointX, (int) pointY);
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                currentEdge = EDGE_NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (status == STATUS_MULTI_TOUCHING) {
                    // TODO if it's a multi touch case.
                    break;
                } else if (status == STATUS_SINGLE) {
                    int dx = (int) (event.getX() - pointX);
                    int dy = (int) (event.getY() - pointY);

                    pointX = event.getX();
                    pointY = event.getY();

                    if (!(dx == 0 && dy == 0)) {
                        switch (currentEdge) {
                            case EDGE_LT:
                                drawableFloat.set(drawableFloat.left + dx, drawableFloat.top + dy,
                                        drawableFloat.right, drawableFloat.bottom);
                                break;
                            case EDGE_RT:
                                drawableFloat.set(drawableFloat.left, drawableFloat.top + dy,
                                        drawableFloat.right + dx, drawableFloat.bottom);
                                break;
                            case EDGE_LB:
                                drawableFloat.set(drawableFloat.left + dx, drawableFloat.top,
                                        drawableFloat.right, drawableFloat.bottom + dy);
                                break;
                            case EDGE_RB:
                                drawableFloat.set(drawableFloat.left, drawableFloat.top,
                                        drawableFloat.right + dx, drawableFloat.bottom + dy);
                                break;
                            case EDGE_MOVE_IN:
                                // We should take a look at user's finger point, which moving every time.
                                isTouchInSquare = drawableFloat.contains((int) event.getX(),
                                        (int) event.getY());
                                if (isTouchInSquare) {
                                    drawableFloat.offset(dx, dy);
                                }
                                break;
                            case EDGE_MOVE_OUT:
                                break;
                            default:
                                break;
                        }
                        drawableFloat.sort();
                        invalidate();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    // according to the initial touch point, we can know which corner user has touched
    public int getTouch(int eventX, int eventY) {
        Rect floatDrawableRect = floatDrawable.getBounds();
        int floatDrawableWidth = floatDrawable.getBorderWidth();
        int floatDrawableHeight = floatDrawable.getBorderHeight();
        if (floatDrawableRect.left <= eventX
                && eventX < (floatDrawableRect.left + floatDrawableWidth)
                && floatDrawableRect.top <= eventY
                && eventY < (floatDrawableRect.top + floatDrawableHeight)) {
            return EDGE_LT;
        } else if ((floatDrawableRect.right - floatDrawableWidth) <= eventX
                && eventX < floatDrawableRect.right
                && floatDrawableRect.top <= eventY
                && eventY < (floatDrawableRect.top + floatDrawableHeight)) {
            return EDGE_RT;
        } else if (floatDrawableRect.left <= eventX
                && eventX < (floatDrawableRect.left + floatDrawableWidth)
                && (floatDrawableRect.bottom - floatDrawableHeight) <= eventY
                && eventY < floatDrawableRect.bottom) {
            return EDGE_LB;
        } else if ((floatDrawableRect.right - floatDrawableWidth) <= eventX
                && eventX < floatDrawableRect.right
                && (floatDrawableRect.bottom - floatDrawableHeight) <= eventY
                && eventY < floatDrawableRect.bottom) {
            return EDGE_RB;
        } else if (floatDrawableRect.contains(eventX, eventY)) {
            return EDGE_MOVE_IN;
        }
        return EDGE_MOVE_OUT;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (drawable == null) {
            return;
        }

        if (drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
            return;
        }

        configureBounds();
        drawable.draw(canvas);
        canvas.save();
        canvas.clipRect(drawableFloat, Region.Op.DIFFERENCE);
        canvas.drawColor(Color.parseColor("#a0000000"));
        canvas.restore();
        floatDrawable.draw(canvas);
    }

    protected void configureBounds() {
        // configureBounds called in onDraw()
        if (isFirst) {
            oriRationWH = ((float) drawable.getIntrinsicWidth())
                    / ((float) drawable.getIntrinsicHeight());

            final float scale = context.getResources().getDisplayMetrics().density;
            int drawableW = (int) (drawable.getIntrinsicWidth() * scale + 0.5f);
            if ((drawable.getIntrinsicHeight() * scale + 0.5f) > getHeight()) {
                drawableW = (int) ((drawable.getIntrinsicWidth() * scale + 0.5f)
                        * (getHeight() / (drawable.getIntrinsicHeight() * scale + 0.5f)));
            }
            int w = Math.min(getWidth(), drawableW);
            int h = (int) (w / oriRationWH);

            int left = (getWidth() - w) / 2;
            int top = (getHeight() - h) / 2;
            int right = left + w;
            int bottom = top + h;

            drawableSrc.set(left, top, right, bottom);
            drawableDst.set(drawableSrc);

            int floatWidth = dipToPx(context, cropWidth);
            int floatHeight = dipToPx(context, cropHeight);

            if (floatWidth > getWidth()) {
                floatWidth = getWidth();
                floatHeight = cropHeight * floatWidth / cropWidth;
            }

            if (floatHeight > getHeight()) {
                floatHeight = getHeight();
                floatWidth = cropWidth * floatHeight / cropHeight;
            }

            int floatLeft = (getWidth() - floatWidth) / 2;
            int floatTop = (getHeight() - floatHeight) / 2;
            drawableFloat.set(floatLeft, floatTop, floatLeft + floatWidth, floatTop + floatHeight);

            isFirst = false;
        } else if (getTouch((int) pointX, (int) pointY) == EDGE_MOVE_IN) {
            if (drawableFloat.left < 0) {
                drawableFloat.right = drawableFloat.width();
                drawableFloat.left = 0;
            }
            if (drawableFloat.top < 0) {
                drawableFloat.bottom = drawableFloat.height();
                drawableFloat.top = 0;
            }
            if (drawableFloat.right > getWidth()) {
                drawableFloat.left = getWidth() - drawableFloat.width();
                drawableFloat.right = getWidth();
            }
            if (drawableFloat.bottom > getHeight()) {
                drawableFloat.top = getHeight() - drawableFloat.height();
                drawableFloat.bottom = getHeight();
            }
            drawableFloat.set(drawableFloat.left, drawableFloat.top, drawableFloat.right,
                    drawableFloat.bottom);
        } else {
            if (drawableFloat.left < 0) {
                drawableFloat.left = 0;
            }
            if (drawableFloat.top < 0) {
                drawableFloat.top = 0;
            }
            if (drawableFloat.right > getWidth()) {
                drawableFloat.right = getWidth();
                drawableFloat.left = getWidth() - drawableFloat.width();
            }
            if (drawableFloat.bottom > getHeight()) {
                drawableFloat.bottom = getHeight();
                drawableFloat.top = getHeight() - drawableFloat.height();
            }
            drawableFloat.set(drawableFloat.left, drawableFloat.top, drawableFloat.right,
                    drawableFloat.bottom);
        }

        drawable.setBounds(drawableDst);
        floatDrawable.setBounds(drawableFloat);
    }

    public Bitmap getCropImage() {
        Bitmap tmpBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(tmpBitmap);
        drawable.draw(canvas);

        Matrix matrix = new Matrix();
        float scale = (float) (drawableSrc.width())
                / (float) (drawableDst.width());
        matrix.postScale(scale, scale);

        Bitmap ret = Bitmap.createBitmap(tmpBitmap, drawableFloat.left,
                drawableFloat.top, drawableFloat.width(),
                drawableFloat.height(), matrix, true);
        tmpBitmap.recycle();
        return ret;
    }

    public int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
