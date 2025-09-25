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

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.databinding.SignatureWidgetBinding;
import org.odk.collect.draw.DrawActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

/**
 * Signature widget.
 *
 * @author BehrAtherton@gmail.com
 */
@SuppressLint("ViewConstructor")
public class SignatureWidget extends BaseImageWidget {
    SignatureWidgetBinding binding;

    public SignatureWidget(Context context, QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry, String tmpImageFilePath, Dependencies dependencies) {
        super(context, prompt, questionMediaManager, waitingForDataRegistry, tmpImageFilePath, dependencies);
        imageClickHandler = new DrawImageClickHandler(DrawActivity.OPTION_SIGNATURE, RequestCodes.SIGNATURE_CAPTURE, org.odk.collect.strings.R.string.signature_capture);

        render();
        updateAnswer();
    }

    @Override
    protected View onCreateWidgetView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = SignatureWidgetBinding.inflate(((Activity) context).getLayoutInflater());
        binding.signButton.setOnClickListener(v -> imageClickHandler.clickImage("signButton"));
        binding.image.setOnClickListener(v -> imageClickHandler.clickImage("viewImage"));

        if (questionDetails.isReadOnly()) {
            binding.signButton.setVisibility(View.GONE);
        }

        errorTextView = binding.errorMessage;
        imageView = binding.image;

        return binding.getRoot();
    }

    @Override
    public Intent addExtrasToIntent(Intent intent) {
        return intent;
    }

    @Override
    protected boolean doesSupportDefaultValues() {
        return true;
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        binding.signButton.setText(getContext().getString(org.odk.collect.strings.R.string.sign_button));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.signButton.setOnLongClickListener(l);
        super.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.signButton.cancelLongPress();
    }
}
