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
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ColorPickerDialog;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import timber.log.Timber;

/**
 * Modified from the FingerPaint example found in The Android Open Source
 * Project.
 *
 * @author BehrAtherton@gmail.com
 */
public class DrawActivity extends Activity {
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
            Timber.e(e);
        }
        if (savepointImage.exists()) {
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

        setTitle(getString(R.string.draw_image));

        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
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
                                "click");
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
                                "click");
                saveAndClose();
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
                                "click");
                reset();
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
                                "click");
                cancelAndClose();
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

    private void saveAndClose() {
        try {
            saveFile(output);
            setResult(Activity.RESULT_OK);
        } catch (FileNotFoundException e) {
            setResult(Activity.RESULT_CANCELED);
        }
        this.finish();
    }

    private void saveFile(File f) throws FileNotFoundException {
        if (drawView.getWidth() == 0 || drawView.getHeight() == 0) {
            // apparently on 4.x, the orientation change notification can occur
            // sometime before the view is rendered. In that case, the view
            // dimensions will not be known.
            Timber.e("View has zero width or zero height");
        } else {
            FileOutputStream fos;
            fos = new FileOutputStream(f);
            Bitmap bitmap = Bitmap.createBitmap(drawView.getBitmapWidth(),
                    drawView.getBitmapHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawView.drawOnCanvas(canvas, 0, 0);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    private void reset() {
        savepointImage.delete();
        if (!OPTION_SIGNATURE.equals(loadOption) && refImage != null
                && refImage.exists()) {
            FileUtils.copyFile(refImage, savepointImage);
        }
        drawView.reset();
        drawView.invalidate();
    }

    private void cancelAndClose() {
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
        String[] items = {getString(R.string.keep_changes),
                getString(R.string.do_not_save)};

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
                                saveAndClose();
                                break;

                            case 1: // discard changes and exit

                                Collect.getInstance()
                                        .getActivityLogger()
                                        .logInstanceAction(this,
                                                "createQuitDrawDialog",
                                                "discardAndExit");
                                cancelAndClose();
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
        private Bitmap bitmap;
        private Canvas canvas;
        private Path currentPath;
        private Path offscreenPath; // Adjusted for position of the bitmap in the view
        private Paint bitmapPaint;
        private File backgroundBitmapFile;
        private float x;
        private float y;

        public DrawView(final Context c) {
            super(c);
            isSignature = false;
            bitmapPaint = new Paint(Paint.DITHER_FLAG);
            currentPath = new Path();
            offscreenPath = new Path();
            backgroundBitmapFile = new File(Collect.TMPDRAWFILE_PATH);
        }

        public DrawView(Context c, boolean isSignature, File f) {
            this(c);
            this.isSignature = isSignature;
            backgroundBitmapFile = f;
        }

        public void reset() {
            DisplayMetrics metrics = getBaseContext().getResources().getDisplayMetrics();
            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;
            resetImage(screenWidth, screenHeight);
        }

        public void resetImage(int w, int h) {
            if (backgroundBitmapFile.exists()) {
                // Because this activity is used in a fixed landscape mode only, sometimes resetImage()
                // is called upon with flipped w/h (before orientation changes have been applied)
                if (w > h) {
                    int temp = w;
                    w = h;
                    h = temp;
                }

                bitmap = FileUtils.getBitmapAccuratelyScaledToDisplay(
                        backgroundBitmapFile, w, h).copy(
                        Bitmap.Config.ARGB_8888, true);
                // bitmap =
                // Bitmap.createScaledBitmap(BitmapFactory.decodeFile(backgroundBitmapFile.getPath()),
                // w, h, true);
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

            this.x = x;
            this.y = y;
        }

        public void drawSignLine() {
            canvas.drawLine(0, (int) (canvas.getHeight() * .7),
                    canvas.getWidth(), (int) (canvas.getHeight() * .7), paint);
        }

        private void touch_move(float x, float y) {
            currentPath.quadTo(this.x, this.y, (x + this.x) / 2, (y + this.y) / 2);
            offscreenPath.quadTo(this.x - getBitmapLeft(), this.y - getBitmapTop(),
                    (x + this.x) / 2 - getBitmapLeft(), (y + this.y) / 2 - getBitmapTop());
            this.x = x;
            this.y = y;
        }

        private void touch_up() {
            if (currentPath.isEmpty()) {
                canvas.drawPoint(x, y, pointPaint);
            } else {
                currentPath.lineTo(x, y);
                offscreenPath.lineTo(x - getBitmapLeft(), y - getBitmapTop());

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
    }

}
