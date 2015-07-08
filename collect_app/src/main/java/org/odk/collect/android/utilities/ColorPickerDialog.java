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

import org.odk.collect.android.R;

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

/**
 * Based heavily upon:
 * http://www.yougli.net/android/a-photoshop-like-color-picker
 * -for-your-android-application/
 *
 * @author BehrAtherton@gmail.com
 * @author yougli@yougli.net
 *
 */
public class ColorPickerDialog extends Dialog {
	public interface OnColorChangedListener {
		void colorChanged(String key, int color);
	}

	private OnColorChangedListener mListener;
	private int mInitialColor, mDefaultColor;
	private String mKey;

	/**
	 * Modified HorizontalScrollView that communicates scroll
	 * actions to interior Vertical scroll view.
	 * From: http://stackoverflow.com/questions/3866499/two-directional-scroll-view
	 *
	 */
	public class WScrollView extends HorizontalScrollView
	{
	    public ScrollView sv;
	    public WScrollView(Context context)
	    {
	        super(context);
	    }

	    public WScrollView(Context context, AttributeSet attrs)
	    {
	        super(context, attrs);
	    }

	    public WScrollView(Context context, AttributeSet attrs, int defStyle)
	    {
	        super(context, attrs, defStyle);
	    }

	    @Override public boolean onTouchEvent(MotionEvent event)
	    {
	        boolean ret = super.onTouchEvent(event);
	        ret = ret | sv.onTouchEvent(event);
	        return ret;
	  }

	    @Override public boolean onInterceptTouchEvent(MotionEvent event)
	    {
	        boolean ret = super.onInterceptTouchEvent(event);
	        ret = ret | sv.onInterceptTouchEvent(event);
	        return ret;
	    }
	}

	private static class ColorPickerView extends View {
		private Paint mPaint;
		private float mCurrentHue = 0;
		private int mCurrentX = 0, mCurrentY = 0;
		private int mCurrentColor, mDefaultColor;
		private final int[] mHueBarColors = new int[258];
		private int[] mMainColors = new int[65536];
		private OnColorChangedListener mListener;

		ColorPickerView(Context c, OnColorChangedListener l, int color,
				int defaultColor) {
			super(c);
			mListener = l;
			mDefaultColor = defaultColor;

			// Get the current hue from the current color and update the main
			// color field
			float[] hsv = new float[3];
			Color.colorToHSV(color, hsv);
			mCurrentHue = hsv[0];
			updateMainColors();

			mCurrentColor = color;

			// Initialize the colors of the hue slider bar
			int index = 0;
			for (float i = 0; i < 256; i += 256 / 42) // Red (#f00) to pink
														// (#f0f)
			{
				mHueBarColors[index] = Color.rgb(255, 0, (int) i);
				index++;
			}
			for (float i = 0; i < 256; i += 256 / 42) // Pink (#f0f) to blue
														// (#00f)
			{
				mHueBarColors[index] = Color.rgb(255 - (int) i, 0, 255);
				index++;
			}
			for (float i = 0; i < 256; i += 256 / 42) // Blue (#00f) to light
														// blue (#0ff)
			{
				mHueBarColors[index] = Color.rgb(0, (int) i, 255);
				index++;
			}
			for (float i = 0; i < 256; i += 256 / 42) // Light blue (#0ff) to
														// green (#0f0)
			{
				mHueBarColors[index] = Color.rgb(0, 255, 255 - (int) i);
				index++;
			}
			for (float i = 0; i < 256; i += 256 / 42) // Green (#0f0) to yellow
														// (#ff0)
			{
				mHueBarColors[index] = Color.rgb((int) i, 255, 0);
				index++;
			}
			for (float i = 0; i < 256; i += 256 / 42) // Yellow (#ff0) to red
														// (#f00)
			{
				mHueBarColors[index] = Color.rgb(255, 255 - (int) i, 0);
				index++;
			}

			// Initializes the Paint that will draw the View
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setTextAlign(Paint.Align.CENTER);
			mPaint.setTextSize(12);
		}

		// Get the current selected color from the hue bar
		private int getCurrentMainColor() {
			int translatedHue = 255 - (int) (mCurrentHue * 255 / 360);
			int index = 0;
			for (float i = 0; i < 256; i += 256 / 42) {
				if (index == translatedHue)
					return Color.rgb(255, 0, (int) i);
				index++;
			}
			for (float i = 0; i < 256; i += 256 / 42) {
				if (index == translatedHue)
					return Color.rgb(255 - (int) i, 0, 255);
				index++;
			}
			for (float i = 0; i < 256; i += 256 / 42) {
				if (index == translatedHue)
					return Color.rgb(0, (int) i, 255);
				index++;
			}
			for (float i = 0; i < 256; i += 256 / 42) {
				if (index == translatedHue)
					return Color.rgb(0, 255, 255 - (int) i);
				index++;
			}
			for (float i = 0; i < 256; i += 256 / 42) {
				if (index == translatedHue)
					return Color.rgb((int) i, 255, 0);
				index++;
			}
			for (float i = 0; i < 256; i += 256 / 42) {
				if (index == translatedHue)
					return Color.rgb(255, 255 - (int) i, 0);
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
						mMainColors[index] = Color.rgb(
								255 - (255 - Color.red(mainColor)) * x / 255,
								255 - (255 - Color.green(mainColor)) * x / 255,
								255 - (255 - Color.blue(mainColor)) * x / 255);
						topColors[x] = mMainColors[index];
					} else
						mMainColors[index] = Color.rgb(
								(255 - y) * Color.red(topColors[x]) / 255,
								(255 - y) * Color.green(topColors[x]) / 255,
								(255 - y) * Color.blue(topColors[x]) / 255);
					index++;
				}
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {
			int translatedHue = 255 - (int) (mCurrentHue * 255 / 360);
			// Display all the colors of the hue bar with lines
			for (int x = 0; x < 256; x++) {
				// If this is not the current selected hue, display the actual
				// color
				if (translatedHue != x) {
					mPaint.setColor(mHueBarColors[x]);
					mPaint.setStrokeWidth(1);
				} else // else display a slightly larger black line
				{
					mPaint.setColor(Color.BLACK);
					mPaint.setStrokeWidth(3);
				}
				canvas.drawLine(x + 10, 0, x + 10, 40, mPaint);
			}

			// Display the main field colors using LinearGradient
			for (int x = 0; x < 256; x++) {
				int[] colors = new int[2];
				colors[0] = mMainColors[x];
				colors[1] = Color.BLACK;
				Shader shader = new LinearGradient(0, 50, 0, 306, colors, null,
						Shader.TileMode.REPEAT);
				mPaint.setShader(shader);
				canvas.drawLine(x + 10, 50, x + 10, 306, mPaint);
			}
			mPaint.setShader(null);

			// Display the circle around the currently selected color in the
			// main field
			if (mCurrentX != 0 && mCurrentY != 0) {
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setColor(Color.BLACK);
				canvas.drawCircle(mCurrentX, mCurrentY, 10, mPaint);
			}

			// Draw a 'button' with the currently selected color
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setColor(mCurrentColor);
			canvas.drawRect(10, 316, 138, 356, mPaint);

			// Set the text color according to the brightness of the color
			mPaint.setColor(getInverseColor(mCurrentColor));
			canvas.drawText(getContext().getString(R.string.ok), 74, 340,
					mPaint);

			// Draw a 'button' with the default color
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setColor(mDefaultColor);
			canvas.drawRect(138, 316, 266, 356, mPaint);

			// Set the text color according to the brightness of the color
			mPaint.setColor(getInverseColor(mDefaultColor));
			canvas.drawText(getContext().getString(R.string.cancel), 202, 340,
					mPaint);
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
			setMeasuredDimension(276, 366);
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

			if ( action == MotionEvent.ACTION_CANCEL ) {
				afterFirstDown = false;
			} else if ( pointerCount == 1 && action == MotionEvent.ACTION_DOWN ) {
				afterFirstDown = true;
				startX = event.getX();
				startY = event.getY();
			} else if ( pointerCount == 1 && action == MotionEvent.ACTION_MOVE && !afterFirstDown ) {
				afterFirstDown = true;
				startX = event.getX();
				startY = event.getY();
			}

			if ( !afterFirstDown || pointerCount != 1 || action != MotionEvent.ACTION_UP ) {
				return true;
			}

			// on an ACTION_UP, we reset the afterFirstDown flag.
			// processing uses the lifting of the finger to choose
			// the color...
			afterFirstDown = false;
			float x = event.getX();
			float y = event.getY();

			if ( Math.abs(x - startX) > 10 && Math.abs(y - startY) > 10 ) {
				// the color location drifted, so it must just be a scrolling action
				// ignore it...
				return ret;
			}

			// If the touch event is located in the hue bar
			if (x > 10 && x < 266 && y > 0 && y < 40) {
				// Update the main field colors
				mCurrentHue = (255 - x) * 360 / 255;
				updateMainColors();

				// Update the current selected color
				int transX = mCurrentX - 10;
				int transY = mCurrentY - 60;
				int index = 256 * (transY - 1) + transX;
				if (index > 0 && index < mMainColors.length)
					mCurrentColor = mMainColors[256 * (transY - 1) + transX];

				// Force the redraw of the dialog
				invalidate();
			}

			// If the touch event is located in the main field
			if (x > 10 && x < 266 && y > 50 && y < 306) {
				mCurrentX = (int) x;
				mCurrentY = (int) y;
				int transX = mCurrentX - 10;
				int transY = mCurrentY - 60;
				int index = 256 * (transY - 1) + transX;
				if (index > 0 && index < mMainColors.length) {
					// Update the current color
					mCurrentColor = mMainColors[index];
					// Force the redraw of the dialog
					invalidate();
				}
			}

			// If the touch event is located in the left button, notify the
			// listener with the current color
			if (x > 10 && x < 138 && y > 316 && y < 356)
				mListener.colorChanged("", mCurrentColor);

			// If the touch event is located in the right button, notify the
			// listener with the default color
			if (x > 138 && x < 266 && y > 316 && y < 356)
				mListener.colorChanged("", mDefaultColor);

			return true;
		}
	}

	public ColorPickerDialog(Context context, OnColorChangedListener listener,
			String key, int initialColor, int defaultColor, String title) {
		super(context);

		mListener = listener;
		mKey = key;
		mInitialColor = initialColor;
		mDefaultColor = defaultColor;
		setTitle(title);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OnColorChangedListener l = new OnColorChangedListener() {
			public void colorChanged(String key, int color) {
				mListener.colorChanged(mKey, color);
				dismiss();
			}
		};

/*BIDIRECTIONAL SCROLLVIEW*/
        ScrollView sv = new ScrollView(this.getContext());
        WScrollView hsv = new WScrollView(this.getContext());
        hsv.sv = sv;
/*END OF BIDIRECTIONAL SCROLLVIEW*/

        sv.addView(new ColorPickerView(getContext(), l, mInitialColor,
				mDefaultColor), new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        hsv.addView(sv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
        setContentView(hsv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        setCanceledOnTouchOutside(true);
	}
}
