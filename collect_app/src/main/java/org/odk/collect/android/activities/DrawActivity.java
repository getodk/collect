/*
 * Copyright (C) 2012 University of Washington
 * Copyright (C) 2007 The Android Open Source Project
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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Modified from the FingerPaint example found in The Android Open Source
 * Project.
 * 
 * @author BehrAtherton@gmail.com
 * 
 */
public class DrawActivity extends Activity {
	public static final String t = "DrawActivity";
	
	public static final String OPTION = "option";
	public static final String OPTION_SIGNATURE = "signature";
	public static final String OPTION_ANNOTATE = "annotate";
	public static final String OPTION_DRAW = "draw";
	public static final String REF_IMAGE = "refImage";
	public static final String EXTRA_OUTPUT = android.provider.MediaStore.EXTRA_OUTPUT;
	public static final String SAVEPOINT_IMAGE = "savepointImage"; // during
																	// restore

	// incoming options...
	private String loadOption = null;
	private File refImage = null;
	private File output = null;
	private File savepointImage = null;

	private Button btnDrawColor;
	private Button btnFinished;
	private Button btnReset;
	private Button btnCancel;
	private Paint paint;
	private Paint pointPaint;
	private int currentColor = 0xFF000000;
	private DrawView drawView;
	private String alertTitleString;
	private AlertDialog alertDialog;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		try {
			saveFile(savepointImage);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if ( savepointImage.exists() ) {
			outState.putString(SAVEPOINT_IMAGE, savepointImage.getAbsolutePath());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Bundle extras = getIntent().getExtras();

		if (extras == null) {
			loadOption = OPTION_DRAW;
			refImage = null;
			savepointImage = new File(Collect.TMPDRAWFILE_PATH);
			savepointImage.delete();
			output = new File(Collect.TMPFILE_PATH);
		} else {
			loadOption = extras.getString(OPTION);
			if (loadOption == null) {
				loadOption = OPTION_DRAW;
			}
			// refImage can also be present if resuming a drawing
			Uri uri = (Uri) extras.get(REF_IMAGE);
			if (uri != null) {
				refImage = new File(uri.getPath());
			}
			String savepoint = extras.getString(SAVEPOINT_IMAGE);
			if (savepoint != null) {
				savepointImage = new File(savepoint);
				if (!savepointImage.exists() && refImage != null
						&& refImage.exists()) {
					FileUtils.copyFile(refImage, savepointImage);
				}
			} else {
				savepointImage = new File(Collect.TMPDRAWFILE_PATH);
				savepointImage.delete();
				if (refImage != null && refImage.exists()) {
					FileUtils.copyFile(refImage, savepointImage);
				}
			}
			uri = (Uri) extras.get(EXTRA_OUTPUT);
			if (uri != null) {
				output = new File(uri.getPath());
			} else {
				output = new File(Collect.TMPFILE_PATH);
			}
		}

		// At this point, we have:
		// loadOption -- type of activity (draw, signature, annotate)
		// refImage -- original image to work with
		// savepointImage -- drawing to use as a starting point (may be copy of
		// original)
		// output -- where the output should be written

		if (OPTION_SIGNATURE.equals(loadOption)) {
			// set landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			alertTitleString = getString(R.string.quit_application,
					getString(R.string.sign_button));
		} else if (OPTION_ANNOTATE.equals(loadOption)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			alertTitleString = getString(R.string.quit_application,
					getString(R.string.markup_image));
		} else {
			alertTitleString = getString(R.string.quit_application,
					getString(R.string.draw_image));
		}

		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.draw_image));

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout v = (RelativeLayout) inflater.inflate(
				R.layout.draw_layout, null);
		LinearLayout ll = (LinearLayout) v.findViewById(R.id.drawViewLayout);

		drawView = new DrawView(this, OPTION_SIGNATURE.equals(loadOption),
				savepointImage);

		ll.addView(drawView);

		setContentView(v);

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

		btnDrawColor = (Button) findViewById(R.id.btnSelectColor);
		btnDrawColor.setTextColor(getInverseColor(currentColor));
		btnDrawColor.getBackground().setColorFilter(currentColor,
				PorterDuff.Mode.SRC_ATOP);
		btnDrawColor.setText(getString(R.string.set_color));
		btnDrawColor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(
								DrawActivity.this,
								"setColorButton",
								"click",
								Collect.getInstance().getFormController()
										.getFormIndex());
				ColorPickerDialog cpd = new ColorPickerDialog(
						DrawActivity.this,
						new ColorPickerDialog.OnColorChangedListener() {
							public void colorChanged(String key, int color) {
								btnDrawColor
										.setTextColor(getInverseColor(color));
								btnDrawColor.getBackground().setColorFilter(
										color, PorterDuff.Mode.SRC_ATOP);
								currentColor = color;
								paint.setColor(color);
								pointPaint.setColor(color);
							}
						}, "key", currentColor, currentColor,
						getString(R.string.select_drawing_color));
				cpd.show();
			}
		});
		btnFinished = (Button) findViewById(R.id.btnFinishDraw);
		btnFinished.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(
								DrawActivity.this,
								"saveAndCloseButton",
								"click",
								Collect.getInstance().getFormController()
										.getFormIndex());
				SaveAndClose();
			}
		});
		btnReset = (Button) findViewById(R.id.btnResetDraw);
		btnReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(
								DrawActivity.this,
								"resetButton",
								"click",
								Collect.getInstance().getFormController()
										.getFormIndex());
				Reset();
			}
		});
		btnCancel = (Button) findViewById(R.id.btnCancelDraw);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(
								DrawActivity.this,
								"cancelAndCloseButton",
								"click",
								Collect.getInstance().getFormController()
										.getFormIndex());
				CancelAndClose();
			}
		});

	}

	private int getInverseColor(int color) {
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);
		int alpha = Color.alpha(color);
		return Color.argb(alpha, 255 - red, 255 - green, 255 - blue);
	}

	private void SaveAndClose() {
		try {
			saveFile(output);
			setResult(Activity.RESULT_OK);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			setResult(Activity.RESULT_CANCELED);
		}
		this.finish();
	}

	private void saveFile(File f) throws FileNotFoundException {
		if ( drawView.getWidth() == 0 || drawView.getHeight() == 0 ) {
			// apparently on 4.x, the orientation change notification can occur
			// sometime before the view is rendered. In that case, the view
			// dimensions will not be known.
			Log.e(t,"view has zero width or zero height");
		} else {
			FileOutputStream fos;
			fos = new FileOutputStream(f);
			Bitmap bitmap = Bitmap.createBitmap(drawView.getWidth(),
					drawView.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			drawView.draw(canvas);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
			try {
				if ( fos != null ) {
					fos.flush();
					fos.close();
				}
			} catch ( Exception e) {
			}
		}
	}

	private void Reset() {
		savepointImage.delete();
		if (!OPTION_SIGNATURE.equals(loadOption) && refImage != null
				&& refImage.exists()) {
			FileUtils.copyFile(refImage, savepointImage);
		}
		drawView.reset();
		drawView.invalidate();
	}

	private void CancelAndClose() {
		setResult(Activity.RESULT_CANCELED);
		this.finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Collect.getInstance().getActivityLogger()
					.logInstanceAction(this, "onKeyDown.KEYCODE_BACK", "quit");
			createQuitDrawDialog();
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (event.isAltPressed()) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(this,
								"onKeyDown.KEYCODE_DPAD_RIGHT", "showNext");
				createQuitDrawDialog();
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (event.isAltPressed()) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(this, "onKeyDown.KEYCODE_DPAD_LEFT",
								"showPrevious");
				createQuitDrawDialog();
				return true;
			}
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Create a dialog with options to save and exit, save, or quit without
	 * saving
	 */
	private void createQuitDrawDialog() {
		String[] items = { getString(R.string.keep_changes),
				getString(R.string.do_not_save) };

		Collect.getInstance().getActivityLogger()
				.logInstanceAction(this, "createQuitDrawDialog", "show");
		alertDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(alertTitleString)
				.setNeutralButton(getString(R.string.do_not_exit),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {

								Collect.getInstance()
										.getActivityLogger()
										.logInstanceAction(this,
												"createQuitDrawDialog",
												"cancel");
								dialog.cancel();

							}
						})
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {

						case 0: // save and exit
							Collect.getInstance()
									.getActivityLogger()
									.logInstanceAction(this,
											"createQuitDrawDialog",
											"saveAndExit");
							SaveAndClose();
							break;

						case 1: // discard changes and exit

							Collect.getInstance()
									.getActivityLogger()
									.logInstanceAction(this,
											"createQuitDrawDialog",
											"discardAndExit");
							CancelAndClose();
							break;

						case 2:// do nothing
							Collect.getInstance()
									.getActivityLogger()
									.logInstanceAction(this,
											"createQuitDrawDialog", "cancel");
							break;
						}
					}
				}).create();
		alertDialog.show();
	}

	public class DrawView extends View {
		private boolean isSignature;
		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Path mCurrentPath;
		private Paint mBitmapPaint;
		private File mBackgroundBitmapFile;

		public DrawView(final Context c) {
			super(c);
			isSignature = false;
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
			mCurrentPath = new Path();
			setBackgroundColor(0xFFFFFFFF);
			mBackgroundBitmapFile = new File(Collect.TMPDRAWFILE_PATH);
		}

		public DrawView(Context c, boolean isSignature, File f) {
			this(c);
			this.isSignature = isSignature;
			mBackgroundBitmapFile = f;
		}

		public void reset() {
			Display display = ((WindowManager) getContext().getSystemService(
					Context.WINDOW_SERVICE)).getDefaultDisplay();
			int screenWidth = display.getWidth();
			int screenHeight = display.getHeight();
			resetImage(screenWidth, screenHeight);
		}

		public void resetImage(int w, int h) {
			if (mBackgroundBitmapFile.exists()) {
				mBitmap = FileUtils.getBitmapScaledToDisplay(
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
				if (isSignature)
					drawSignLine();
			}
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			resetImage(w, h);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(0xFFAAAAAA);
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			canvas.drawPath(mCurrentPath, paint);
		}

		private float mX, mY;

		private void touch_start(float x, float y) {
			mCurrentPath.reset();
			mCurrentPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		public void drawSignLine() {
			mCanvas.drawLine(0, (int) (mCanvas.getHeight() * .7),
					mCanvas.getWidth(), (int) (mCanvas.getHeight() * .7), paint);
		}

		private void touch_move(float x, float y) {
			mCurrentPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}

		private void touch_up() {
			if (mCurrentPath.isEmpty()) {
				mCanvas.drawPoint(mX, mY, pointPaint);
			} else {
				mCurrentPath.lineTo(mX, mY);
				// commit the path to our offscreen
				mCanvas.drawPath(mCurrentPath, paint);
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
