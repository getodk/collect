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
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaUtils;

import java.io.File;

import timber.log.Timber;

/**
 * Free drawing widget.
 *
 * @author BehrAtherton@gmail.com
 */
@SuppressLint("ViewConstructor")
public class DrawWidget extends QuestionWidget implements FileWidget {

    private Button drawButton;
    private String binaryName;
    private String instanceFolder;

    @Nullable
    private ImageView imageView;

    private TextView errorTextView;

    public DrawWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        errorTextView = new TextView(context);
        errorTextView.setId(QuestionWidget.newUniqueId());
        errorTextView.setText(R.string.selected_invalid_image);

        instanceFolder = Collect.getInstance().getFormController()
                .getInstancePath().getParent();

        drawButton = getSimpleButton(getContext().getString(R.string.draw_image));
        drawButton.setEnabled(!prompt.isReadOnly());
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "drawButton", "click",
                                formEntryPrompt.getIndex());
                launchDrawActivity();
            }
        });

        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(drawButton);
        answerLayout.addView(errorTextView);

        if (formEntryPrompt.isReadOnly()) {
            drawButton.setVisibility(View.GONE);
        }
        errorTextView.setVisibility(View.GONE);

        // retrieve answer from data model and update ui
        binaryName = prompt.getAnswerText();

        // Only add the imageView if the user has signed
        if (binaryName != null) {
            imageView = new ImageView(getContext());
            imageView.setId(QuestionWidget.newUniqueId());
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;

            File f = new File(instanceFolder + File.separator + binaryName);

            if (f.exists()) {
                Bitmap bmp = FileUtils.getBitmapScaledToDisplay(f,
                        screenHeight, screenWidth);
                if (bmp == null) {
                    errorTextView.setVisibility(View.VISIBLE);
                }
                imageView.setImageBitmap(bmp);
            } else {
                imageView.setImageBitmap(null);
            }

            imageView.setPadding(10, 10, 10, 10);
            imageView.setAdjustViewBounds(true);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Collect.getInstance()
                            .getActivityLogger()
                            .logInstanceAction(this, "viewImage", "click",
                                    formEntryPrompt.getIndex());
                    launchDrawActivity();
                }
            });

            answerLayout.addView(imageView);
        }
        addAnswerView(answerLayout);

    }

    private void launchDrawActivity() {
        errorTextView.setVisibility(View.GONE);
        Intent i = new Intent(getContext(), DrawActivity.class);
        i.putExtra(DrawActivity.OPTION, DrawActivity.OPTION_DRAW);
        // copy...
        if (binaryName != null) {
            File f = new File(instanceFolder + File.separator + binaryName);
            i.putExtra(DrawActivity.REF_IMAGE, Uri.fromFile(f));
        }
        i.putExtra(DrawActivity.EXTRA_OUTPUT,
                Uri.fromFile(new File(Collect.TMPFILE_PATH)));

        try {
            Collect.getInstance().getFormController()
                    .setIndexWaitingForData(formEntryPrompt.getIndex());
            ((Activity) getContext()).startActivityForResult(i,
                    FormEntryActivity.DRAW_IMAGE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            "draw image"), Toast.LENGTH_SHORT).show();
            Collect.getInstance().getFormController()
                    .setIndexWaitingForData(null);
        }
    }

    @Override
    public void deleteFile() {
        // get the file path and delete the file
        String name = binaryName;
        // clean up variables
        binaryName = null;
        // delete from media provider
        int del = MediaUtils.deleteImageFileFromMediaProvider(instanceFolder
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

        // reset buttons
        drawButton.setText(getContext().getString(R.string.draw_image));
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
    public void setBinaryData(Object answer) {
        // you are replacing an answer. delete the previous image using the
        // content provider.
        if (binaryName != null) {
            deleteFile();
        }

        File newImage = (File) answer;
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

        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public boolean isWaitingForBinaryData() {
        return formEntryPrompt.getIndex().equals(
                Collect.getInstance().getFormController()
                        .getIndexWaitingForData());
    }

    @Override
    public void cancelWaitingForBinaryData() {
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        drawButton.setOnLongClickListener(l);
        if (imageView != null) {
            imageView.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        drawButton.cancelLongPress();
        if (imageView != null) {
            imageView.cancelLongPress();
        }
    }
}
