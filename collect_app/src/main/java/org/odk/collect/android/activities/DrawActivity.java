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
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.AnimateUtils;
import org.odk.collect.android.utilities.ColorPickerDialog;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.views.DrawView;

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
public class DrawActivity extends AppCompatActivity {
    public static final String OPTION = "option";
    public static final String OPTION_SIGNATURE = "signature";
    public static final String OPTION_ANNOTATE = "annotate";
    public static final String OPTION_DRAW = "draw";
    public static final String REF_IMAGE = "refImage";
    public static final String EXTRA_OUTPUT = android.provider.MediaStore.EXTRA_OUTPUT;
    public static final String SAVEPOINT_IMAGE = "savepointImage"; // during
    // restore

    private FloatingActionButton fabActions;

    // incoming options...
    private String loadOption = null;
    private File refImage = null;
    private File output = null;
    private File savepointImage = null;

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
        setContentView(R.layout.draw_layout);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fabActions = (FloatingActionButton) findViewById(R.id.fab_actions);
        final FloatingActionButton fabSetColor = (FloatingActionButton) findViewById(R.id.fab_set_color);
        final CardView cardViewSetColor = (CardView) findViewById(R.id.cv_set_color);
        final FloatingActionButton fabSaveAndClose = (FloatingActionButton) findViewById(R.id.fab_save_and_close);
        final CardView cardViewSaveAndClose = (CardView) findViewById(R.id.cv_save_and_close);
        final FloatingActionButton fabClear = (FloatingActionButton) findViewById(R.id.fab_clear);
        final CardView cardViewClear = (CardView) findViewById(R.id.cv_clear);

        fabActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int status = Integer.parseInt(view.getTag().toString());
                if (status == 0) {
                    status = 1;
                    fabActions.animate().rotation(45).setInterpolator(new AccelerateDecelerateInterpolator())
                            .setDuration(100).start();

                    AnimateUtils.scaleInAnimation(fabSetColor, 50, 150, new OvershootInterpolator(), true);
                    AnimateUtils.scaleInAnimation(cardViewSetColor, 50, 150, new OvershootInterpolator(), true);
                    AnimateUtils.scaleInAnimation(fabSaveAndClose, 100, 150, new OvershootInterpolator(), true);
                    AnimateUtils.scaleInAnimation(cardViewSaveAndClose, 100, 150, new OvershootInterpolator(), true);
                    AnimateUtils.scaleInAnimation(fabClear, 150, 150, new OvershootInterpolator(), true);
                    AnimateUtils.scaleInAnimation(cardViewClear, 150, 150, new OvershootInterpolator(), true);
                } else {
                    status = 0;
                    fabActions.animate().rotation(0).setInterpolator(new AccelerateDecelerateInterpolator())
                            .setDuration(100).start();

                    fabSetColor.setVisibility(View.INVISIBLE);
                    cardViewSetColor.setVisibility(View.INVISIBLE);
                    fabSaveAndClose.setVisibility(View.INVISIBLE);
                    cardViewSaveAndClose.setVisibility(View.INVISIBLE);
                    fabClear.setVisibility(View.INVISIBLE);
                    cardViewClear.setVisibility(View.INVISIBLE);
                }
                view.setTag(status);
            }
        });

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
            alertTitleString = getString(R.string.quit_application,
                    getString(R.string.sign_button));
        } else if (OPTION_ANNOTATE.equals(loadOption)) {
            alertTitleString = getString(R.string.quit_application,
                    getString(R.string.markup_image));
        } else {
            alertTitleString = getString(R.string.quit_application,
                    getString(R.string.draw_image));
        }

        drawView = (DrawView) findViewById(R.id.drawView);
        drawView.setupView(this, OPTION_SIGNATURE.equals(loadOption), savepointImage);
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

<<<<<<< HEAD
    public void clear(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            fabActions.performClick();
            reset();
=======
    public class DrawView extends View {
        private boolean isSignature;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mCurrentPath;
        private Path mOffscreenPath; // Adjusted for position of the bitmap in the view
        private Paint mBitmapPaint;
        private File mBackgroundBitmapFile;

        public DrawView(final Context c) {
            super(c);
            isSignature = false;
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mCurrentPath = new Path();
            mOffscreenPath = new Path();
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
            // Because this activity is used in a fixed landscape mode only, sometimes resetImage()
            // is called upon with flipped w/h (before orientation changes have been applied)
            if (w > h) {
                int temp = w;
                w = h;
                h = temp;
            }

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

        private float mX, mY;

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

        public int getBitmapHeight(){
            return mBitmap.getHeight();
        }

        public int getBitmapWidth(){
            return mBitmap.getWidth();
>>>>>>> smap-merge
        }
    }

    public void close(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            fabActions.performClick();
            saveAndClose();
        }
    }

    public void setColor(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            fabActions.performClick();
            ColorPickerDialog cpd = new ColorPickerDialog(
                    DrawActivity.this,
                    new ColorPickerDialog.OnColorChangedListener() {
                        public void colorChanged(String key, int color) {
                            drawView.setColor(color);
                        }
                    }, "key", drawView.getColor(), drawView.getColor(),
                    getString(R.string.select_drawing_color));
            cpd.show();
        }
    }
}
