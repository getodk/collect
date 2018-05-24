/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
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
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaManager;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.widgets.interfaces.FileWidget;

import java.io.File;

import timber.log.Timber;

public abstract class BaseImageWidget extends QuestionWidget implements FileWidget {
    @Nullable
    protected ImageView imageView;
    protected String binaryName;
    protected TextView errorTextView;
    protected LinearLayout answerLayout;

    protected ImageClickHandler imageClickHandler;
    protected ExternalImageCaptureHandler imageCaptureHandler;

    public BaseImageWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    @Override
    public IAnswerData getAnswer() {
        return binaryName == null ? null : new StringData(binaryName);
    }

    @Override
    public void clearAnswer() {
        deleteFile();
        if (imageView != null) {
            imageView.setImageDrawable(null);
        }

        errorTextView.setVisibility(View.GONE);
    }

    @Override
    public void deleteFile() {
        MediaManager
                .INSTANCE
                .markOriginalFileOrDelete(getFormEntryPrompt().getIndex().toString(),
                        getInstanceFolder() + File.separator + binaryName);
        binaryName = null;
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
            values.put(MediaStore.Images.Media.TITLE, newImage.getName());
            values.put(MediaStore.Images.Media.DISPLAY_NAME, newImage.getName());
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, newImage.getAbsolutePath());

            MediaManager
                    .INSTANCE
                    .replaceRecentFileForQuestion(getFormEntryPrompt().getIndex().toString(), newImage.getAbsolutePath());

            Uri imageURI = getContext().getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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
    public void setOnLongClickListener(OnLongClickListener l) {
        if (imageView != null) {
            imageView.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        if (imageView != null) {
            imageView.cancelLongPress();
        }
    }

    protected void setUpBinary() {
        if (binaryName != null) {
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;

            File f = new File(getInstanceFolder() + File.separator + binaryName);

            Bitmap bmp = null;
            if (f.exists()) {
                bmp = FileUtils.getBitmapScaledToDisplay(f, screenHeight, screenWidth);
                if (bmp == null) {
                    errorTextView.setVisibility(View.VISIBLE);
                }
            }

            imageView = getAnswerImageView(bmp);
            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (imageClickHandler != null) {
                        imageClickHandler.clickImage("viewImage");
                    }
                }
            });

        answerLayout.addView(imageView);
        }
    }

    protected void setUpLayout() {
        errorTextView = new TextView(getContext());
        errorTextView.setId(ViewIds.generateViewId());
        errorTextView.setText(R.string.selected_invalid_image);

        answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);

        binaryName = getFormEntryPrompt().getAnswerText();
    }

    /**
     * Enables a subclass to add extras to the intent before launching the draw activity.
     *
     * @param intent to add extras
     * @return intent with added extras
     */
    public abstract Intent addExtrasToIntent(@NonNull Intent intent);

    /**
     * Interface for Clicking on Images
     */
    protected interface ImageClickHandler {
        void clickImage(String context);
    }

    /**
     * Class to implement launching of viewing an image Activity
     */
    protected class ViewImageClickHandler implements ImageClickHandler {

        @Override
        public void clickImage(String context) {
            Collect.getInstance().getActivityLogger().logInstanceAction(this, context,
                    "click", getFormEntryPrompt().getIndex());
            Intent i = new Intent("android.intent.action.VIEW");
            Uri uri = MediaUtils.getImageUriFromMediaProvider(
                    getInstanceFolder() + File.separator + binaryName);
            if (uri != null) {
                Timber.i("setting view path to: %s", uri.toString());
                i.setDataAndType(uri, "image/*");


                try {
                    getContext().startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(),
                            getContext().getString(R.string.activity_not_found,
                                    getContext().getString(R.string.view_image)),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Class to implement launching of drawing image Activity when clicked
     */
    protected class DrawImageClickHandler implements ImageClickHandler {

        private final String drawOption;
        private final int requestCode;
        private final int stringResourceId;

        public DrawImageClickHandler(String option, final int code, final int resourceId) {
            drawOption = option;
            requestCode = code;
            stringResourceId = resourceId;
        }

        @Override
        public void clickImage(String context) {
            if (Collect.allowClick()) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, context, "click",
                                getFormEntryPrompt().getIndex());
                launchDrawActivity();
            }
        }

        private void launchDrawActivity() {
            errorTextView.setVisibility(View.GONE);
            Intent i = new Intent(getContext(), DrawActivity.class);
            i.putExtra(DrawActivity.OPTION, drawOption);
            // copy...
            if (binaryName != null) {
                File f = new File(getInstanceFolder() + File.separator + binaryName);
                i.putExtra(DrawActivity.REF_IMAGE, Uri.fromFile(f));
            }
            i.putExtra(DrawActivity.EXTRA_OUTPUT, Uri.fromFile(new File(Collect.TMPFILE_PATH)));
            i = addExtrasToIntent(i);
            launchActivityForResult(i, requestCode, stringResourceId);
        }
    }

    /**
     * Interface for choosing or capturing a new image
     */
    protected interface ExternalImageCaptureHandler {
        void captureImage(Intent intent, int requestCode, int stringResource);

        void chooseImage(int stringResource);
    }

    /**
     * Class for launching the image capture or choose image activities
     */
    protected class ImageCaptureHandler implements ExternalImageCaptureHandler {

        @Override
        public void captureImage(Intent intent, final int requestCode, int stringResource) {
            launchActivityForResult(intent, requestCode, stringResource);
        }

        @Override
        public void chooseImage(@IdRes final int stringResource) {
            Collect.getInstance().getActivityLogger().logInstanceAction(this, "chooseButton",
                    "click", getFormEntryPrompt().getIndex());
            errorTextView.setVisibility(View.GONE);
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            launchActivityForResult(i, ApplicationConstants.RequestCodes.IMAGE_CHOOSER, stringResource);
        }
    }

    /**
     * Standard method for launching an Activity.
     *
     * @param intent - The Intent to start
     * @param resourceCode - Code to return when Activity exits
     * @param errorStringResource - String resource for error toast
     */
    protected void launchActivityForResult(Intent intent, final int resourceCode, final int errorStringResource) {
        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(intent, resourceCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.activity_not_found, getContext().getString(errorStringResource)),
                    Toast.LENGTH_SHORT).show();
            cancelWaitingForData();
        }
    }
}
