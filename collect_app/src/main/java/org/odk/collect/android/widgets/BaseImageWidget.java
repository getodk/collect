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

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaManager;
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
                .getInstance()
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
                    .getInstance()
                    .markRecentFile(getFormEntryPrompt().getIndex().toString(), newImage.getAbsolutePath());

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
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
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
            answerLayout.addView(imageView);
        }
    }

    protected abstract void onImageClick();

    protected void setUpLayout() {
        errorTextView = new TextView(getContext());
        errorTextView.setId(ViewIds.generateViewId());
        errorTextView.setText(R.string.selected_invalid_image);

        answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);

        binaryName = getFormEntryPrompt().getAnswerText();
    }
}
