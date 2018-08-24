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
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.common.collect.ImmutableList;
import com.rarepebble.colorpicker.ColorPickerView;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.IconMenuListAdapter;
import org.odk.collect.android.adapters.model.IconMenuItem;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.AnimateUtils;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.views.DrawView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import timber.log.Timber;

/**
 * Modified from the FingerPaint example found in The Android Open Source
 * Project.
 *
 * @author BehrAtherton@gmail.com
 */
public class DrawActivity extends CollectAbstractActivity {
    public static final String OPTION = "option";
    public static final String OPTION_SIGNATURE = "signature";
    public static final String OPTION_ANNOTATE = "annotate";
    public static final String OPTION_DRAW = "draw";
    public static final String REF_IMAGE = "refImage";
    public static final String SCREEN_ORIENTATION = "screenOrientation";
    public static final String EXTRA_OUTPUT = android.provider.MediaStore.EXTRA_OUTPUT;
    public static final String SAVEPOINT_IMAGE = "savepointImage"; // during
    // restore

    private FloatingActionButton fabActions;

    // incoming options...
    private String loadOption;
    private File refImage;
    private File output;
    private File savepointImage;

    private DrawView drawView;
    private String alertTitleString;
    private AlertDialog alertDialog;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            saveFile(savepointImage);
        } catch (FileNotFoundException e) {
            Timber.d(e);
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

        fabActions = findViewById(R.id.fab_actions);
        final FloatingActionButton fabSetColor = findViewById(R.id.fab_set_color);
        final CardView cardViewSetColor = findViewById(R.id.cv_set_color);
        final FloatingActionButton fabSaveAndClose = findViewById(R.id.fab_save_and_close);
        final CardView cardViewSaveAndClose = findViewById(R.id.cv_save_and_close);
        final FloatingActionButton fabClear = findViewById(R.id.fab_clear);
        final CardView cardViewClear = findViewById(R.id.cv_clear);

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
            if (extras.getInt(SCREEN_ORIENTATION) == 1) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
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

        drawView = findViewById(R.id.drawView);
        drawView.setupView(OPTION_SIGNATURE.equals(loadOption));
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

        Collect.getInstance().getActivityLogger()
                .logInstanceAction(this, "createQuitDrawDialog", "show");

        ListView listView = DialogUtils.createActionListView(this);

        List<IconMenuItem> items;
        items = ImmutableList.of(new IconMenuItem(R.drawable.ic_save, R.string.keep_changes),
                new IconMenuItem(R.drawable.ic_delete, R.string.do_not_save));

        final IconMenuListAdapter adapter = new IconMenuListAdapter(this, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IconMenuItem item = (IconMenuItem) adapter.getItem(position);
                if (item.getTextResId() == R.string.keep_changes) {
                    Collect.getInstance()
                            .getActivityLogger()
                            .logInstanceAction(this,
                                    "createQuitDrawDialog",
                                    "saveAndExit");
                    saveAndClose();
                } else {
                    Collect.getInstance()
                            .getActivityLogger()
                            .logInstanceAction(this,
                                    "createQuitDrawDialog",
                                    "discardAndExit");
                    cancelAndClose();
                }
                alertDialog.dismiss();
            }
        });
        alertDialog = new AlertDialog.Builder(this)
                .setTitle(alertTitleString)
                .setPositiveButton(getString(R.string.do_not_exit),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Collect.getInstance()
                                        .getActivityLogger()
                                        .logInstanceAction(this,
                                                "createQuitDrawDialog", "cancel");
                            }
                        })
                .setView(listView).create();
        alertDialog.show();
    }

    public void clear(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            fabActions.performClick();
            reset();
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

            final ColorPickerView picker = new ColorPickerView(this);
            picker.setColor(drawView.getColor());
            picker.showAlpha(false);
            picker.showHex(false);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setView(picker)
                    .setPositiveButton(R.string.ok, (dialog, which) -> drawView.setColor(picker.getColor()))
                    .show();
        }
    }
}
