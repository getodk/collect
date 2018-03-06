/*
 * Copyright (C) 2012 University of Washington
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

package org.odk.collect.android.utilities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import org.odk.collect.android.R;

/**
 * Based heavily upon:
 * http://www.yougli.net/android/a-photoshop-like-color-picker
 * -for-your-android-application/
 *
 * @author BehrAtherton@gmail.com
 * @author yougli@yougli.net
 */
public class ColorPickerDialog extends Dialog {
    public interface OnColorChangedListener {
        void colorChanged(String key, int color);
    }

    private static final float SCALE_FACTOR = 3;
    private OnColorChangedListener listener;
    private int initialColor;
    private int defaultColor;
    private String key;

    /**
     * Modified HorizontalScrollView that communicates scroll
     * actions to interior Vertical scroll view.
     * From: http://stackoverflow.com/questions/3866499/two-directional-scroll-view
     */
    public class WScrollView extends HorizontalScrollView {
        public ScrollView sv;

        public WScrollView(Context context) {
            super(context);
        }

        public WScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public WScrollView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean ret = super.onTouchEvent(event);
            ret = ret | sv.onTouchEvent(event);
            return ret;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            boolean ret = super.onInterceptTouchEvent(event);
            ret = ret | sv.onInterceptTouchEvent(event);
            return ret;
        }
    }

    private static class ColorPickerView extends View {
        private Paint paint;
        private float currentHue = 0;
        private int currentX = 0;
        private int currentY = 0;
        private int currentColor;
        private int defaultColor;
        private final int[] hueBarColors = new int[258];
        private int[] mainColors = new int[65536];
        private OnColorChangedListener listener;

        ColorPickerView(Context c, OnColorChangedListener l, int color,
                        int defaultColor) {
            super(c);
            listener = l;
            this.defaultColor = defaultColor;

            // Get the current hue from the current color and update the main
            // color field
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            currentHue = hsv[0];
            updateMainColors();

            currentColor = color;

            // Initialize the colors of the hue slider bar
            int index = 0;
            for (float i = 0; i < 256; i += 256 / 42) {
                // Red (#f00) to pink (#f0f)
                hueBarColors[index] = Color.rgb(255, 0, (int) i);
                index++;
            }
            for (float i = 0; i < 256; i += 256 / 42) {
                // Pink (#f0f) to blue (#00f)
                hueBarColors[index] = Color.rgb(255 - (int) i, 0, 255);
                index++;
            }
            for (float i = 0; i < 256; i += 256 / 42) {
                // Blue (#00f) to light blue (#0ff)
                hueBarColors[index] = Color.rgb(0, (int) i, 255);
                index++;
            }
            for (float i = 0; i < 256; i += 256 / 42) {
                // Light blue (#0ff) to green (#0f0)
                hueBarColors[index] = Color.rgb(0, 255, 255 - (int) i);
                index++;
            }
            for (float i = 0; i < 256; i += 256 / 42) {
                // Green (#0f0) to yellow (#ff0)
                hueBarColors[index] = Color.rgb((int) i, 255, 0);
                index++;
            }
            for (float i = 0; i < 256; i += 256 / 42) {
                // Yellow (#ff0) to red (#f00)
                hueBarColors[index] = Color.rgb(255, 255 - (int) i, 0);
                index++;
            }

            // Initializes the Paint that will draw the View
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(12 * SCALE_FACTOR);
        }

        // Get the current selected color from the hue bar
        private int getCurrentMainColor() {
            int translatedHue = 255 - (int) (currentHue * 255 / 360);
            int index = 0;
            for (float i = 0; i < 256; i += 256 / 42) {
                if (index == translatedHue) {
                    return Color.rgb(255, 0, (int) i);
                }
                index++;
            }
            for (float i = 0; i < 256; i += 256 / 42) {
                if (index == translatedHue) {
                    return Color.rgb(255 - (int) i, 0, 255);
                }
                index++;
            }
            for (float i = 0; i < 256; i += 256 / 42) {
                if (index == translatedHue) {
                    return Color.rgb(0, (int) i, 255);
                }
                index++;
            }
            for (float i = 0; i < 256; i += 256 / 42) {
                if (index == translatedHue) {
                    return Color.rgb(0, 255, 255 - (int) i);
                }
                index++;
            }
            for (float i = 0; i < 256; i += 256 / 42) {
                if (index == translatedHue) {
                    return Color.rgb((int) i, 255, 0);
                }
                index++;
            }
            for (float i = 0; i < 256; i += 256 / 42) {
                if (index == translatedHue) {
                    return Color.rgb(255, 255 - (int) i, 0);
                }
                index++;
            }
            return Color.RED;
        }

        // Update the main field colors depending on the current selected hue
        private void updateMainColors() {
            int mainColor = getCurrentMainColor();
            int index = 0;
            int[] topColors = new int[256];
            for (int y = 0; y < 256; y++) {
                for (int x = 0; x < 256; x++) {
                    if (y == 0) {
                        mainColors[index] = Color.rgb(
                                255 - (255 - Color.red(mainColor)) * x / 255,
                                255 - (255 - Color.green(mainColor)) * x / 255,
                                255 - (255 - Color.blue(mainColor)) * x / 255);
                        topColors[x] = mainColors[index];
                    } else {
                        mainColors[index] = Color.rgb(
                                (255 - y) * Color.red(topColors[x]) / 255,
                                (255 - y) * Color.green(topColors[x]) / 255,
                                (255 - y) * Color.blue(topColors[x]) / 255);
                    }
                    index++;
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int translatedHue = 255 - (int) (currentHue * 255 / 360);
            // Display all the colors of the hue bar with lines
            for (int x = 0; x < 256; x++) {
                // If this is not the current selected hue, display the actual
                // color
                if (translatedHue != x) {
                    paint.setColor(hueBarColors[x]);
                    paint.setStrokeWidth(1);
                } else {
                    // else display a slightly larger black line
                    paint.setColor(Color.BLACK);
                    paint.setStrokeWidth(3);
                }
                canvas.drawLine((x + 10)* SCALE_FACTOR, 0* SCALE_FACTOR, (x + 10) * SCALE_FACTOR, 40 * SCALE_FACTOR, paint);
            }

            // Display the main field colors using LinearGradient
            for (int x = 0; x < 256; x++) {
                int[] colors = new int[2];
                colors[0] = mainColors[x];
                colors[1] = Color.BLACK;
                Shader shader = new LinearGradient(0, 50 * SCALE_FACTOR, 0, 306 * SCALE_FACTOR, colors, null,
                        Shader.TileMode.REPEAT);
                paint.setShader(shader);
                canvas.drawLine((x + 10) * SCALE_FACTOR, 50 * SCALE_FACTOR, (x + 10) * SCALE_FACTOR, 306 * SCALE_FACTOR, paint);
            }
            paint.setShader(null);

            // Display the circle around the currently selected color in the
            // main field
            if (currentX != 0 && currentY != 0) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.BLACK);
                canvas.drawCircle(currentX, currentY, 10, paint);
            }

            // Draw a 'button' with the currently selected color
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(currentColor);
            canvas.drawRect(10 * SCALE_FACTOR, 316 * SCALE_FACTOR, 138 * SCALE_FACTOR, 356 * SCALE_FACTOR, paint);

            // Set the text color according to the brightness of the color
            paint.setColor(getInverseColor(currentColor));
            canvas.drawText(getContext().getString(R.string.ok), 74 * SCALE_FACTOR, 340 * SCALE_FACTOR,
                    paint);

            // Draw a 'button' with the default color
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(defaultColor);
            canvas.drawRect(138 * SCALE_FACTOR, 316 * SCALE_FACTOR, 266 * SCALE_FACTOR, 356 * SCALE_FACTOR, paint);

            // Set the text color according to the brightness of the color
            paint.setColor(getInverseColor(defaultColor));
            canvas.drawText(getContext().getString(R.string.cancel), 202 * SCALE_FACTOR, 340 * SCALE_FACTOR,
                    paint);
        }

        private int getInverseColor(int color) {
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            int alpha = Color.alpha(color);
            return Color.argb(alpha, 255 - red, 255 - green, 255 - blue);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension((int) (276 * SCALE_FACTOR), (int) (366 * SCALE_FACTOR));
        }

        private boolean afterFirstDown = false;
        private float startX;
        private float startY;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // allow scrolling...
            boolean ret = super.onTouchEvent(event);

            int action = event.getAction();
            int pointerCount = event.getPointerCount();

            if (action == MotionEvent.ACTION_CANCEL) {
                afterFirstDown = false;
            } else if (pointerCount == 1 && action == MotionEvent.ACTION_DOWN) {
                afterFirstDown = true;
                startX = event.getX();
                startY = event.getY();
            } else if (pointerCount == 1 && action == MotionEvent.ACTION_MOVE && !afterFirstDown) {
                afterFirstDown = true;
                startX = event.getX();
                startY = event.getY();
            }

            if (!afterFirstDown || pointerCount != 1 || action != MotionEvent.ACTION_UP) {
                return true;
            }

            // on an ACTION_UP, we reset the afterFirstDown flag.
            // processing uses the lifting of the finger to choose
            // the color...
            afterFirstDown = false;
            float x = event.getX();
            float y = event.getY();

            if (Math.abs(x - startX) > 10 && Math.abs(y - startY) > 10) {
                // the color location drifted, so it must just be a scrolling action
                // ignore it...
                return ret;
            }

            // If the touch event is located in the hue bar
            if (x > 10 * SCALE_FACTOR && x < 266 * SCALE_FACTOR && y > 0 * SCALE_FACTOR && y < 40 * SCALE_FACTOR) {
                // Update the main field colors
                currentHue = (255 - x / SCALE_FACTOR) * 360 / 255;
                updateMainColors();

                // Update the current selected color
                int transX = (int) ((currentX / SCALE_FACTOR - 10));
                int transY = (int) ((currentY / SCALE_FACTOR - 60));
                int index = 256 * (transY - 1) + transX;
                if (index > 0 && index < mainColors.length) {
                    currentColor = mainColors[256 * (transY - 1) + transX];
                }

                // Force the redraw of the dialog
                invalidate();
            }

            // If the touch event is located in the main field
            if (x > 10 * SCALE_FACTOR && x < 266 * SCALE_FACTOR && y > 50 * SCALE_FACTOR && y < 306 * SCALE_FACTOR) {
                currentX = (int) x;
                currentY = (int) y;
                int transX = (int) ((currentX / SCALE_FACTOR - 10));
                int transY = (int) ((currentY / SCALE_FACTOR - 60));
                int index = 256 * (transY - 1) + transX;
                if (index > 0 && index < mainColors.length) {
                    // Update the current color
                    currentColor = mainColors[index];
                    // Force the redraw of the dialog
                    invalidate();
                }
            }

            // If the touch event is located in the left button, notify the
            // listener with the current color
            if (x > 10 * SCALE_FACTOR && x < 138 * SCALE_FACTOR && y > 316 * SCALE_FACTOR && y < 356 * SCALE_FACTOR) {
                listener.colorChanged("", currentColor);
            }

            // If the touch event is located in the right button, notify the
            // listener with the default color
            if (x > 138 * SCALE_FACTOR && x < 266 * SCALE_FACTOR && y > 316 * SCALE_FACTOR && y < 356 * SCALE_FACTOR) {
                listener.colorChanged("", defaultColor);
            }

            return true;
        }
    }

    public ColorPickerDialog(Context context, OnColorChangedListener listener,
                             String key, int initialColor, int defaultColor, String title) {
        super(context);

        this.listener = listener;
        this.key = key;
        this.initialColor = initialColor;
        this.defaultColor = defaultColor;
        setTitle(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(String key, int color) {
                listener.colorChanged(ColorPickerDialog.this.key, color);
                dismiss();
            }
        };

        /*BIDIRECTIONAL SCROLLVIEW*/
        ScrollView sv = new ScrollView(this.getContext());
        WScrollView hsv = new WScrollView(this.getContext());
        hsv.sv = sv;
        /*END OF BIDIRECTIONAL SCROLLVIEW*/

        sv.addView(new ColorPickerView(getContext(), l, initialColor,
                        defaultColor),
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        hsv.addView(sv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        setContentView(hsv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        setCanceledOnTouchOutside(true);
    }
}
