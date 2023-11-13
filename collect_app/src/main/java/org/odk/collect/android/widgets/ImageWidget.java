/*
 * Copyright (C) 2009 University of Washington
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

import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createSimpleButton;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.formentry.questions.WidgetViewUtils;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.interfaces.ButtonClickListener;
import org.odk.collect.android.widgets.utilities.ImageCaptureIntentCreator;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.odk.collect.androidshared.system.CameraUtils;
import org.odk.collect.selfiecamera.CaptureSelfieActivity;

import java.util.Locale;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

@SuppressLint("ViewConstructor")
public class ImageWidget extends BaseImageWidget implements ButtonClickListener {

    Button captureButton;
    Button chooseButton;

    private boolean selfie;

    public ImageWidget(Context context, final QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry, String tmpImageFilePath) {
        super(context, prompt, questionMediaManager, waitingForDataRegistry, tmpImageFilePath);
        render();

        imageClickHandler = new ViewImageClickHandler();
        imageCaptureHandler = new ImageCaptureHandler();
        setUpLayout();
        updateAnswer();
        addAnswerView(answerLayout, WidgetViewUtils.getStandardMargin(context));
    }

    @Override
    protected void setUpLayout() {
        super.setUpLayout();

        String appearance = getFormEntryPrompt().getAppearanceHint();
        selfie = Appearances.isFrontCameraAppearance(getFormEntryPrompt());

        captureButton = createSimpleButton(getContext(), R.id.capture_image, questionDetails.isReadOnly(), getContext().getString(org.odk.collect.strings.R.string.capture_image),  this);

        chooseButton = createSimpleButton(getContext(), R.id.choose_image, questionDetails.isReadOnly(), getContext().getString(org.odk.collect.strings.R.string.choose_image), this);

        answerLayout.addView(captureButton);
        answerLayout.addView(chooseButton);
        answerLayout.addView(errorTextView);
        answerLayout.addView(imageView);

        hideButtonsIfNeeded(appearance);
    }

    @Override
    public Intent addExtrasToIntent(Intent intent) {
        return intent;
    }

    @Override
    protected boolean doesSupportDefaultValues() {
        return false;
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        // reset buttons
        captureButton.setText(getContext().getString(org.odk.collect.strings.R.string.capture_image));
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
        if (buttonId == R.id.capture_image) {
            getPermissionsProvider().requestCameraPermission((Activity) getContext(), this::captureImage);
        } else if (buttonId == R.id.choose_image) {
            imageCaptureHandler.chooseImage(org.odk.collect.strings.R.string.choose_image);
        }
    }

    private void hideButtonsIfNeeded(String appearance) {
        if (selfie || ((appearance != null
                && appearance.toLowerCase(Locale.ENGLISH).contains(Appearances.NEW)))) {
            chooseButton.setVisibility(View.GONE);
        }
    }

    private void captureImage() {
        if (selfie && new CameraUtils().isFrontCameraAvailable(getContext())) {
            Intent intent = new Intent(getContext(), CaptureSelfieActivity.class);
            intent.putExtra(CaptureSelfieActivity.EXTRA_TMP_PATH, new StoragePathProvider().getOdkDirPath(StorageSubdirectory.CACHE));
            imageCaptureHandler.captureImage(intent, RequestCodes.MEDIA_FILE_PATH, org.odk.collect.strings.R.string.capture_image);
        } else {
            Intent intent = ImageCaptureIntentCreator.imageCaptureIntent(getFormEntryPrompt(), getContext(), tmpImageFilePath);
            imageCaptureHandler.captureImage(intent, RequestCodes.IMAGE_CAPTURE, org.odk.collect.strings.R.string.capture_image);
        }
    }

}
