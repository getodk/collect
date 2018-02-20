/*
 * Copyright (C) 2013 University of Washington
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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.MediaUtils;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * Widget that allows user to invoke the aligned-image camera to take pictures and add them to the
 * form.
 * Modified to launch the Aligned-image camera app.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author mitchellsundt@gmail.com
 * @author Mitchell Tyler Lee
 */
@SuppressLint("ViewConstructor")
public class AlignedImageWidget extends BaseImageWidget {
    private static final String ODK_CAMERA_TAKE_PICTURE_INTENT_COMPONENT =
            "org.opendatakit.camera.TakePicture";

    private static final String ODK_CAMERA_INTENT_PACKAGE = "org.opendatakit.camera";

    private static final String RETAKE_OPTION_EXTRA = "retakeOption";

    private static final String DIMENSIONS_EXTRA = "dimensions";

    private static final String FILE_PATH_EXTRA = "filePath";

    private Button captureButton;
    private Button chooseButton;

    private String instanceFolder;
    
    private final int[] iarray = new int[6];

    public AlignedImageWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        setUpLayout();
        setUpBinary();
        addAnswerView(answerLayout);
    }

    @Override
    public void onImageClick() {
        getActivityLogger().logInstanceAction(this, "viewButton",
                "click", getFormEntryPrompt().getIndex());
        Intent i = new Intent("android.intent.action.VIEW");
        Uri uri = MediaUtils.getImageUriFromMediaProvider(
                instanceFolder + File.separator + binaryName);
        if (uri != null) {
            Timber.i("setting view path to: %s", uri.toString());
            i.setDataAndType(uri, "image/*");
            try {
                getContext().startActivity(i);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(),
                        getContext().getString(R.string.activity_not_found,
                                "view image"),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void setUpLayout() {
        super.setUpLayout();
        String appearance = getFormEntryPrompt().getAppearanceHint();
        String alignments = appearance.substring(appearance.indexOf(':') + 1);
        String[] splits = alignments.split(" ");
        if (splits.length != 6) {
            Timber.w("Only have %d alignment values", splits.length);
        }
        for (int i = 0; i < splits.length; i++) {
            iarray[i] = Integer.parseInt(splits[i]);
        }

        instanceFolder = getInstanceFolder();

        captureButton = getSimpleButton(getContext().getString(R.string.capture_image), R.id.capture_image);
        captureButton.setEnabled(!getFormEntryPrompt().isReadOnly());

        chooseButton = getSimpleButton(getContext().getString(R.string.choose_image), R.id.choose_image);
        chooseButton.setEnabled(!getFormEntryPrompt().isReadOnly());

        answerLayout.addView(captureButton);
        answerLayout.addView(chooseButton);
        answerLayout.addView(errorTextView);

        // and hide the capture and choose button if read-only
        if (getFormEntryPrompt().isReadOnly()) {
            captureButton.setVisibility(View.GONE);
            chooseButton.setVisibility(View.GONE);
        }
        errorTextView.setVisibility(View.GONE);
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        // reset buttons
        captureButton.setText(getContext().getString(R.string.capture_image));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        captureButton.setOnLongClickListener(l);
        chooseButton.setOnLongClickListener(l);
        super.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
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
        Collect collect = Collect.getInstance();
        collect.getActivityLogger().logInstanceAction(this, "captureButton",
                "click", getFormEntryPrompt().getIndex());

        errorTextView.setVisibility(View.GONE);

        Intent i = new Intent();
        i.setComponent(new ComponentName(ODK_CAMERA_INTENT_PACKAGE,
                ODK_CAMERA_TAKE_PICTURE_INTENT_COMPONENT));
        i.putExtra(FILE_PATH_EXTRA, Collect.CACHE_PATH);
        i.putExtra(DIMENSIONS_EXTRA, iarray);
        i.putExtra(RETAKE_OPTION_EXTRA, false);

        // We give the camera an absolute filename/path where to put the
        // picture because of bug:
        // http://code.google.com/p/android/issues/detail?id=1480
        // The bug appears to be fixed in Android 2.0+, but as of feb 2,
        // 2010, G1 phones only run 1.6. Without specifying the path the
        // images returned by the camera in 1.6 (and earlier) are ~1/4
        // the size. boo.

        // if this gets modified, the onActivityResult in
        // FormEntyActivity will also need to be updated.
        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.ALIGNED_IMAGE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.activity_not_found,
                            "aligned image capture"),
                    Toast.LENGTH_SHORT).show();
            cancelWaitingForData();
        }
    }

    private void chooseImage() {
        getActivityLogger().logInstanceAction(this, "chooseButton", "click", getFormEntryPrompt().getIndex());
        errorTextView.setVisibility(View.GONE);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");

        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.IMAGE_CHOOSER);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.activity_not_found, "choose image"),
                    Toast.LENGTH_SHORT).show();
            cancelWaitingForData();
        }
    }
}
