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

public class CropImageView extends View {
    // Touch point
    private float mX_1 = 0;
    private float mY_1 = 0;
    // Touch events
    private final int STATUS_SINGLE = 1;
    private final int STATUS_MULTI_START = 2;
    private final int STATUS_MULTI_TOUCHING = 3;
    // Current status
    private int mStatus = STATUS_SINGLE;
    // Default height & width
    private int cropWidth;
    private int cropHeight;
    // Four points of the float layer
    private final int EDGE_LT = 1;
    private final int EDGE_RT = 2;
    private final int EDGE_LB = 3;
    private final int EDGE_RB = 4;
    private final int EDGE_MOVE_IN = 5;
    private final int EDGE_MOVE_OUT = 6;
    private final int EDGE_NONE = 7;

    public int currentEdge = EDGE_NONE;

    protected float oriRationWH = 0;

    protected Drawable mDrawable;
    protected FloatDrawable mFloatDrawable;

    protected Rect mDrawableSrc = new Rect();
    protected Rect mDrawableDst = new Rect();
    protected Rect mDrawableFloat = new Rect();
    protected boolean isFirst = true;
    private boolean isTouchInSquare = true;

    protected Context mContext;
    private Bitmap mBitmap;

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
        this.mContext = context;
        try {
            // use a software way to draw views.
            this.setLayerType(LAYER_TYPE_SOFTWARE, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mFloatDrawable = new FloatDrawable(context);
    }

    public void setDrawable(Bitmap bitmap, int cropWidth, int cropHeight) {
        mBitmap = bitmap;
        this.mDrawable = new BitmapDrawable(bitmap);
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

        if (mBitmap != null) {
            if ((mBitmap.getHeight() > heightSize) && (mBitmap.getHeight() > mBitmap.getWidth())) {
                widthSize = heightSize * mBitmap.getWidth() / mBitmap.getHeight();
            } else if ((mBitmap.getWidth() > widthSize) && (mBitmap.getWidth() > mBitmap.getHeight())) {
                heightSize = widthSize * mBitmap.getHeight() / mBitmap.getWidth();
            } else {
                heightSize = mBitmap.getHeight();
                widthSize = mBitmap.getWidth();
            }
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getPointerCount() > 1) {
            if (mStatus == STATUS_SINGLE) {
                mStatus = STATUS_MULTI_START;
            } else if (mStatus == STATUS_MULTI_START) {
                mStatus = STATUS_MULTI_TOUCHING;
            }
        } else {
            if (mStatus == STATUS_MULTI_START
                    || mStatus == STATUS_MULTI_TOUCHING) {
                mX_1 = event.getX();
                mY_1 = event.getY();
            }

            mStatus = STATUS_SINGLE;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mX_1 = event.getX();
                mY_1 = event.getY();
                currentEdge = getTouch((int) mX_1, (int) mY_1);
                break;

            case MotionEvent.ACTION_UP:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                currentEdge = EDGE_NONE;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mStatus == STATUS_MULTI_TOUCHING) {
                    // TODO if it's a multi touch case.
                    break;
                } else if (mStatus == STATUS_SINGLE) {
                    int dx = (int) (event.getX() - mX_1);
                    int dy = (int) (event.getY() - mY_1);

                    mX_1 = event.getX();
                    mY_1 = event.getY();

                    // TODO If touch point is out of a border, we should turn back。
                    // We should add this improvement here. but may cause a bug when
                    // touch point is moving to fast.
//                    if (mX_1 > getWidth() || mX_1 < 0 || mY_1 > getHeight() || mY_1 < 0) {
//                        break;
//                    }
                    if (!(dx == 0 && dy == 0)) {
                        switch (currentEdge) {
                            case EDGE_LT:
                                mDrawableFloat.set(mDrawableFloat.left + dx,
                                        mDrawableFloat.top + dy,
                                        mDrawableFloat.right,
                                        mDrawableFloat.bottom);
                                break;

                            case EDGE_RT:
                                mDrawableFloat.set(mDrawableFloat.left,
                                        mDrawableFloat.top + dy,
                                        mDrawableFloat.right + dx,
                                        mDrawableFloat.bottom);
                                break;

                            case EDGE_LB:
                                mDrawableFloat.set(mDrawableFloat.left + dx,
                                        mDrawableFloat.top,
                                        mDrawableFloat.right,
                                        mDrawableFloat.bottom + dy);
                                break;

                            case EDGE_RB:
                                mDrawableFloat.set(mDrawableFloat.left,
                                        mDrawableFloat.top,
                                        mDrawableFloat.right + dx,
                                        mDrawableFloat.bottom + dy);
                                break;

                            case EDGE_MOVE_IN:
                                // We should take a look at user's finger point, which moving every time.
                                isTouchInSquare = mDrawableFloat.contains((int) event.getX(),
                                        (int) event.getY());
                                if (isTouchInSquare) {
                                    mDrawableFloat.offset(dx, dy);
                                }
                                break;

                            case EDGE_MOVE_OUT:
                                break;
                        }
                        mDrawableFloat.sort();
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    // according to the initial touch point, we can know which corner user has touched
    public int getTouch(int eventX, int eventY) {
        Rect mFloatDrawableRect = mFloatDrawable.getBounds();
        int mFloatDrawableWidth = mFloatDrawable.getBorderWidth();
        int mFloatDrawableHeight = mFloatDrawable.getBorderHeight();
        if (mFloatDrawableRect.left <= eventX
                && eventX < (mFloatDrawableRect.left + mFloatDrawableWidth)
                && mFloatDrawableRect.top <= eventY
                && eventY < (mFloatDrawableRect.top + mFloatDrawableHeight)) {
            return EDGE_LT;
        } else if ((mFloatDrawableRect.right - mFloatDrawableWidth) <= eventX
                && eventX < mFloatDrawableRect.right
                && mFloatDrawableRect.top <= eventY
                && eventY < (mFloatDrawableRect.top + mFloatDrawableHeight)) {
            return EDGE_RT;
        } else if (mFloatDrawableRect.left <= eventX
                && eventX < (mFloatDrawableRect.left + mFloatDrawableWidth)
                && (mFloatDrawableRect.bottom - mFloatDrawableHeight) <= eventY
                && eventY < mFloatDrawableRect.bottom) {
            return EDGE_LB;
        } else if ((mFloatDrawableRect.right - mFloatDrawableWidth) <= eventX
                && eventX < mFloatDrawableRect.right
                && (mFloatDrawableRect.bottom - mFloatDrawableHeight) <= eventY
                && eventY < mFloatDrawableRect.bottom) {
            return EDGE_RB;
        } else if (mFloatDrawableRect.contains(eventX, eventY)) {
            return EDGE_MOVE_IN;
        }
        return EDGE_MOVE_OUT;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mDrawable == null) {
            return;
        }

        if (mDrawable.getIntrinsicWidth() == 0 || mDrawable.getIntrinsicHeight() == 0) {
            return;
        }

        configureBounds();
        mDrawable.draw(canvas);
        canvas.save();
        canvas.clipRect(mDrawableFloat, Region.Op.DIFFERENCE);
        canvas.drawColor(Color.parseColor("#a0000000"));
        canvas.restore();
        mFloatDrawable.draw(canvas);
    }

    protected void configureBounds() {
        // configureBounds called in onDraw()
        if (isFirst) {
            oriRationWH = ((float) mDrawable.getIntrinsicWidth())
                    / ((float) mDrawable.getIntrinsicHeight());

            final float scale = mContext.getResources().getDisplayMetrics().density;
            int mDrawableW = (int) (mDrawable.getIntrinsicWidth() * scale + 0.5f);
            if ((mDrawable.getIntrinsicHeight() * scale + 0.5f) > getHeight()) {
                mDrawableW = (int) ((mDrawable.getIntrinsicWidth() * scale + 0.5f)
                        * (getHeight() / (mDrawable.getIntrinsicHeight() * scale + 0.5f)));
            }
            int w = Math.min(getWidth(), mDrawableW);
            int h = (int) (w / oriRationWH);

            int left = (getWidth() - w) / 2;
            int top = (getHeight() - h) / 2;
            int right = left + w;
            int bottom = top + h;

            mDrawableSrc.set(left, top, right, bottom);
            mDrawableDst.set(mDrawableSrc);

            int floatWidth = dipToPx(mContext, cropWidth);
            int floatHeight = dipToPx(mContext, cropHeight);

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
            mDrawableFloat.set(floatLeft, floatTop, floatLeft + floatWidth, floatTop + floatHeight);

            isFirst = false;
        } else if (getTouch((int) mX_1, (int) mY_1) == EDGE_MOVE_IN) {
            if (mDrawableFloat.left < 0) {
                mDrawableFloat.right = mDrawableFloat.width();
                mDrawableFloat.left = 0;
            }
            if (mDrawableFloat.top < 0) {
                mDrawableFloat.bottom = mDrawableFloat.height();
                mDrawableFloat.top = 0;
            }
            if (mDrawableFloat.right > getWidth()) {
                mDrawableFloat.left = getWidth() - mDrawableFloat.width();
                mDrawableFloat.right = getWidth();
            }
            if (mDrawableFloat.bottom > getHeight()) {
                mDrawableFloat.top = getHeight() - mDrawableFloat.height();
                mDrawableFloat.bottom = getHeight();
            }
            mDrawableFloat.set(mDrawableFloat.left, mDrawableFloat.top, mDrawableFloat.right,
                    mDrawableFloat.bottom);
        } else {
            if (mDrawableFloat.left < 0) {
                mDrawableFloat.left = 0;
            }
            if (mDrawableFloat.top < 0) {
                mDrawableFloat.top = 0;
            }
            if (mDrawableFloat.right > getWidth()) {
                mDrawableFloat.right = getWidth();
                mDrawableFloat.left = getWidth() - mDrawableFloat.width();
            }
            if (mDrawableFloat.bottom > getHeight()) {
                mDrawableFloat.bottom = getHeight();
                mDrawableFloat.top = getHeight() - mDrawableFloat.height();
            }
            mDrawableFloat.set(mDrawableFloat.left, mDrawableFloat.top, mDrawableFloat.right,
                    mDrawableFloat.bottom);
        }

        mDrawable.setBounds(mDrawableDst);
        mFloatDrawable.setBounds(mDrawableFloat);
    }

    public Bitmap getCropImage() {
        Bitmap tmpBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(tmpBitmap);
        mDrawable.draw(canvas);

        Matrix matrix = new Matrix();
        float scale = (float) (mDrawableSrc.width())
                / (float) (mDrawableDst.width());
        matrix.postScale(scale, scale);

        Bitmap ret = Bitmap.createBitmap(tmpBitmap, mDrawableFloat.left,
                mDrawableFloat.top, mDrawableFloat.width(),
                mDrawableFloat.height(), matrix, true);
        tmpBitmap.recycle();
        return ret;
    }

    public int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
