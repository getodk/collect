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

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.widgets.interfaces.BaseImageWidget;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * Image widget that supports annotations on the image.
 *
 * @author BehrAtherton@gmail.com
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class AnnotateWidget extends QuestionWidget implements BaseImageWidget {

    private Button captureButton;
    private Button chooseButton;
    private Button annotateButton;

    @Nullable
    private ImageView imageView;

    private String binaryName;

    private TextView errorTextView;

    private int screenOrientation;

    public AnnotateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        errorTextView = new TextView(context);
        errorTextView.setId(ViewIds.generateViewId());
        errorTextView.setText(R.string.selected_invalid_image);

        captureButton = getSimpleButton(getContext().getString(R.string.capture_image), R.id.capture_image);
        captureButton.setEnabled(!prompt.isReadOnly());

        chooseButton = getSimpleButton(getContext().getString(R.string.choose_image), R.id.choose_image);
        chooseButton.setEnabled(!prompt.isReadOnly());

        annotateButton = getSimpleButton(getContext().getString(R.string.markup_image), R.id.markup_image);
        annotateButton.setEnabled(false);
        annotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "annotateButton", "click",
                                getFormEntryPrompt().getIndex());
                launchAnnotateActivity();
            }
        });

        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);

        answerLayout.addView(captureButton);
        answerLayout.addView(chooseButton);
        answerLayout.addView(annotateButton);
        answerLayout.addView(errorTextView);

        // and hide the capture, choose and annotate button if read-only
        if (prompt.isReadOnly()) {
            captureButton.setVisibility(View.GONE);
            chooseButton.setVisibility(View.GONE);
            annotateButton.setVisibility(View.GONE);
        }
        errorTextView.setVisibility(View.GONE);

        // retrieve answer from data model and update ui
        binaryName = prompt.getAnswerText();

        // Only add the imageView if the user has taken a picture
        if (binaryName != null) {
            if (!prompt.isReadOnly()) {
                annotateButton.setEnabled(true);
            }
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;

            File f = new File(getInstanceFolder() + File.separator + binaryName);

            Bitmap bmp = null;
            if (f.exists()) {
                bmp = FileUtils.getBitmapScaledToDisplay(f,
                        screenHeight, screenWidth);
                if (bmp == null) {
                    errorTextView.setVisibility(View.VISIBLE);
                } else if (bmp.getHeight() > bmp.getWidth()) {
                    screenOrientation = 1; // portrait
                }
            }
            imageView = getAnswerImageView(bmp);
            answerLayout.addView(imageView);
        }
        addAnswerView(answerLayout);
    }

    @Override
    public void onImageClick() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "viewImage", "click",
                        getFormEntryPrompt().getIndex());
        launchAnnotateActivity();
    }

    private void launchAnnotateActivity() {
        errorTextView.setVisibility(View.GONE);
        Intent i = new Intent(getContext(), DrawActivity.class);
        i.putExtra(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE);
        // copy...
        if (binaryName != null) {
            File f = new File(getInstanceFolder() + File.separator + binaryName);
            i.putExtra(DrawActivity.REF_IMAGE, Uri.fromFile(f));
        }
        i.putExtra(DrawActivity.EXTRA_OUTPUT, Uri.fromFile(new File(Collect.TMPFILE_PATH)));
        i.putExtra(DrawActivity.SCREEN_ORIENTATION, screenOrientation);

        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.ANNOTATE_IMAGE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            "annotate image"), Toast.LENGTH_SHORT).show();
            cancelWaitingForData();
        }
    }

    @Override
    public void deleteFile() {
        // get the file path and delete the file
        String name = binaryName;
        // clean up variables
        binaryName = null;
        // delete from media provider
        int del = MediaUtils.deleteImageFileFromMediaProvider(getInstanceFolder()
                + File.separator + name);
        Timber.i("Deleted %d rows from media content provider", del);
    }

    @Override
    public void clearAnswer() {
        // remove the file
        deleteFile();
        if (imageView != null) {
            imageView.setImageBitmap(null);
        }

        errorTextView.setVisibility(View.GONE);
        if (!getFormEntryPrompt().isReadOnly()) {
            annotateButton.setEnabled(false);
        }

        // reset buttons
        captureButton.setText(getContext().getString(R.string.capture_image));
    }

    @Override
    public IAnswerData getAnswer() {
        if (binaryName != null) {
            return new StringData(binaryName);
        } else {
            return null;
        }
    }

    @Override
    public void setBinaryData(Object newImageObj) {
        // you are replacing an answer. delete the previous image using the
        // content provider.
        if (binaryName != null) {
            deleteFile();
        }

        File newImage = (File) newImageObj;
        if (newImage.exists()) {
            // Add the new image to the Media content provider so that the
            // viewing is fast in Android 2.0+
            ContentValues values = new ContentValues(6);
            values.put(Images.Media.TITLE, newImage.getName());
            values.put(Images.Media.DISPLAY_NAME, newImage.getName());
            values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(Images.Media.MIME_TYPE, "image/jpeg");
            values.put(Images.Media.DATA, newImage.getAbsolutePath());

            Uri imageURI = getContext().getContentResolver().insert(
                    Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageURI != null) {
                Timber.i("Inserting image returned uri = %s", imageURI.toString());
            }

            binaryName = newImage.getName();
            Timber.i("Setting current answer to %s", newImage.getName());

        } else {
            Timber.e("NO IMAGE EXISTS at: %s", newImage.getAbsolutePath());
        }
    }

    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        captureButton.setOnLongClickListener(l);
        chooseButton.setOnLongClickListener(l);
        annotateButton.setOnLongClickListener(l);
        if (imageView != null) {
            imageView.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
        annotateButton.cancelLongPress();
        if (imageView != null) {
            imageView.cancelLongPress();
        }
    }

    @Override
    public void onButtonClick(int buttonId) {
        switch (buttonId) {
            case R.id.capture_image:
                captureImage();
                break;
            case R.id.choose_image:
                chooseImage();
                break;
        }
    }

    private void captureImage() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "captureButton", "click",
                        getFormEntryPrompt().getIndex());
        errorTextView.setVisibility(View.GONE);
        Intent i = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        // We give the camera an absolute filename/path where to put the
        // picture because of bug:
        // http://code.google.com/p/android/issues/detail?id=1480
        // The bug appears to be fixed in Android 2.0+, but as of feb 2,
        // 2010, G1 phones only run 1.6. Without specifying the path the
        // images returned by the camera in 1.6 (and earlier) are ~1/4
        // the size. boo.

        // if this gets modified, the onActivityResult in
        // FormEntyActivity will also need to be updated.
        i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(new File(Collect.TMPFILE_PATH)));
        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            "image capture"), Toast.LENGTH_SHORT)
                    .show();
            cancelWaitingForData();
        }
    }

    private void chooseImage() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "chooseButton", "click",
                        getFormEntryPrompt().getIndex());
        errorTextView.setVisibility(View.GONE);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");

        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.IMAGE_CHOOSER);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            "choose image"), Toast.LENGTH_SHORT).show();
            cancelWaitingForData();
        }
    }
}
