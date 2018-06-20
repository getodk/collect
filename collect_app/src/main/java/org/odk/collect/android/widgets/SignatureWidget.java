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

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.DrawActivity;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * Signature widget.
 *
 * @author BehrAtherton@gmail.com
 */
public class SignatureWidget extends BaseImageWidget {

    private Button signButton;

    public SignatureWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        imageClickHandler = new DrawImageClickHandler(DrawActivity.OPTION_SIGNATURE, RequestCodes.SIGNATURE_CAPTURE, R.string.signature_capture);
        setUpLayout();
        setUpBinary();
        addAnswerView(answerLayout);
    }

    @Override
    protected void setUpLayout() {
        super.setUpLayout();
        signButton = getSimpleButton(getContext().getString(R.string.sign_button));
        signButton.setEnabled(!getFormEntryPrompt().isReadOnly());

        answerLayout.addView(signButton);
        answerLayout.addView(errorTextView);

        // and hide the sign button if read-only
        if (getFormEntryPrompt().isReadOnly()) {
            signButton.setVisibility(View.GONE);
        }
        errorTextView.setVisibility(View.GONE);
    }

    @Override
    public Intent addExtrasToIntent(Intent intent) {
        return intent;
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        // reset buttons
        signButton.setText(getContext().getString(R.string.sign_button));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        signButton.setOnLongClickListener(l);
        super.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        signButton.cancelLongPress();
    }

    @Override
    public void onButtonClick(int buttonId) {
        imageClickHandler.clickImage("signButton");
    }
}
