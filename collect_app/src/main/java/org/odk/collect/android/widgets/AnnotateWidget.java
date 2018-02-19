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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;
import org.odk.collect.android.application.Collect;

import java.io.File;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * Image widget that supports annotations on the image.
 *
 * @author BehrAtherton@gmail.com
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class AnnotateWidget extends BaseImageWidget {

    private Button captureButton;
    private Button chooseButton;
    private Button annotateButton;

    public AnnotateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        setUpLayout();
        setUpBinary();
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

    @Override
    protected void setUpLayout() {
        super.setUpLayout();
        captureButton = getSimpleButton(getContext().getString(R.string.capture_image), R.id.capture_image);
        captureButton.setEnabled(!getFormEntryPrompt().isReadOnly());

        chooseButton = getSimpleButton(getContext().getString(R.string.choose_image), R.id.choose_image);
        chooseButton.setEnabled(!getFormEntryPrompt().isReadOnly());

        annotateButton = getSimpleButton(getContext().getString(R.string.markup_image), R.id.markup_image);
        annotateButton.setEnabled(!(binaryName == null || getFormEntryPrompt().isReadOnly()));
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

        answerLayout.addView(captureButton);
        answerLayout.addView(chooseButton);
        answerLayout.addView(annotateButton);
        answerLayout.addView(errorTextView);

        // and hide the capture, choose and annotate button if read-only
        if (getFormEntryPrompt().isReadOnly()) {
            captureButton.setVisibility(View.GONE);
            chooseButton.setVisibility(View.GONE);
            annotateButton.setVisibility(View.GONE);
        }
        errorTextView.setVisibility(View.GONE);
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
        i.putExtra(DrawActivity.SCREEN_ORIENTATION, calculateScreenOrientation());

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
    public void clearAnswer() {
        super.clearAnswer();
        if (!getFormEntryPrompt().isReadOnly()) {
            annotateButton.setEnabled(false);
        }

        // reset buttons
        captureButton.setText(getContext().getString(R.string.capture_image));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        captureButton.setOnLongClickListener(l);
        chooseButton.setOnLongClickListener(l);
        annotateButton.setOnLongClickListener(l);
        super.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
        annotateButton.cancelLongPress();
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

    private int calculateScreenOrientation() {
        Bitmap bmp = null;
        if (imageView != null) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        }

        return bmp != null && bmp.getHeight() > bmp.getWidth() ?
                SCREEN_ORIENTATION_PORTRAIT : SCREEN_ORIENTATION_LANDSCAPE;
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
