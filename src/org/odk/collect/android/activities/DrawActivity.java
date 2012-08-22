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

package org.odk.collect.android.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ColorPickerDialog;
import org.odk.collect.android.utilities.FileUtils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * 
 * @author BehrAtherton@gmail.com
 *
 */
public class DrawActivity extends Activity {
    Button btnDrawColor;
    Button btnFinished;
    Button btnCancel;
	Paint mPaint;
	Paint mPointPaint;
    int currentColor = 0xFF000000;
    private DrawView drawView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Bundle extras = getIntent().getExtras();
        String loadOption = "";
        if(extras != null)
        {
        	loadOption = extras.getString("option");
        }
        boolean isSignature = false;
        File img = null;
        if("signature".equals(loadOption)) {
        	// set landscape
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        	isSignature = true;
        }
        else if ("annotate".equals(loadOption) && extras != null) {
        	String refimg = extras.getString("refImage");
          	//Log.i("DrawActivity", "refimg: "+refimg);
          	if(refimg != null)
          		img = new File(refimg);
        }
        
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.draw_image));
        
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout v = (RelativeLayout)inflater.inflate(R.layout.draw_layout, null);
        LinearLayout ll = (LinearLayout)v.findViewById(R.id.drawViewLayout);
        
        if(img != null) {
        	drawView = new DrawView(this, img);
        }
        else {
	    	drawView = new DrawView(this);
	        drawView.setBackgroundColor(0xFFFFFFFF);
        }
        if(isSignature)
        	drawView.setIsSignature(true);
        ll.addView(drawView);
	        
        setContentView(v);

        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(currentColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(10);
        
        mPointPaint = new Paint();
        mPointPaint.setAntiAlias(true);
        mPointPaint.setDither(true);
        mPointPaint.setColor(currentColor);
        mPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPointPaint.setStrokeWidth(10);
        
        
		btnDrawColor = (Button)findViewById(R.id.btnSelectColor);
		btnDrawColor.setTextColor(ColorStateList.valueOf(currentColor));
		btnDrawColor.setBackgroundColor(currentColor);
		btnDrawColor.setText("Set Color");
		btnDrawColor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ColorPickerDialog cpd = new ColorPickerDialog(DrawActivity.this, new ColorPickerDialog.OnColorChangedListener() {
							public void colorChanged(String key, int color) {
								btnDrawColor.setTextColor(ColorStateList.valueOf(color));
								btnDrawColor.setBackgroundColor(color);
								currentColor = color;
								mPaint.setColor(color);
								mPointPaint.setColor(color);
							}
						}, "key", currentColor, currentColor, "Select Drawing Color");
				cpd.show();
			}
		});
		if(isSignature)
			btnDrawColor.setEnabled(false);
		//else
		//	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		btnFinished = (Button)findViewById(R.id.btnFinishDraw);
		btnFinished.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SaveAndClose();
			}
		});
		btnCancel = (Button)findViewById(R.id.btnCancelDraw);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CancelAndClose();
			}
		});

    }
    
    private void SaveAndClose() {
    	FileOutputStream fos;
		try {
			fos = new FileOutputStream(Collect.TMPFILE_PATH);
	    	Bitmap  bitmap = Bitmap.createBitmap( drawView.getWidth(), drawView.getHeight(), Bitmap.Config.ARGB_8888);
	    	Canvas canvas = new Canvas(bitmap);
	    	drawView.draw(canvas); 
	    	bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos); 
			setResult(Activity.RESULT_OK);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			setResult(Activity.RESULT_CANCELED);
		}
    	this.finish();
    }
    
    private void CancelAndClose() {
		setResult(Activity.RESULT_CANCELED);
    	this.finish();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }

    public class DrawView extends View {
    	private boolean isSignature = false;
        private Bitmap  mBitmap;
        private Canvas  mCanvas;
        private Path mCurrentPath;
        private Paint   mBitmapPaint;
        File mBackgroundBitmapFile = null;

        public DrawView(final Context c) {
            super(c);
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mCurrentPath = new Path();
        }
        
        public void setIsSignature(boolean b) {
			isSignature = b;
		}

		public DrawView(Context c, File f) {
        	super(c);
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mCurrentPath = new Path();
            mBackgroundBitmapFile = f;
        }

        @Override
       protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if(mBackgroundBitmapFile != null)
            {
            	mBitmap = FileUtils.getBitmapScaledToDisplay(mBackgroundBitmapFile, w, h).copy(Bitmap.Config.ARGB_8888, true);
            	//mBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(mBackgroundBitmapFile.getPath()), w, h, true);
            	mCanvas = new Canvas(mBitmap);
            }
            else {
            	mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	            mCanvas = new Canvas(mBitmap);
	            mCanvas.drawColor(0xFFFFFFFF);
	            if(isSignature)
	            	drawSignLine();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFAAAAAA);
           	canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(mCurrentPath, mPaint);
        }

        private float mX, mY;

        private void touch_start(float x, float y) {
        	mCurrentPath.reset();
        	mCurrentPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        
        public void drawSignLine() {
        	mCanvas.drawLine(0, (int)(mCanvas.getHeight()*.7), mCanvas.getWidth(), (int)(mCanvas.getHeight()*.7), mPaint);
        }
        private void touch_move(float x, float y) {
        	mCurrentPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
        private void touch_up() {
        	if(mCurrentPath.isEmpty()) {
        		mCanvas.drawPoint(mX, mY, mPointPaint);
        	}
        	else {
	        	mCurrentPath.lineTo(mX, mY);
	            // commit the path to our offscreen
	            mCanvas.drawPath(mCurrentPath, mPaint);
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
        
    }

}
