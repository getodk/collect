/*
 * Copyright (C) 2013 Nafundi
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
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.BearingActivity;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;

/**
 * BearingWidget is the widget that allows the user to get a compass heading.
 *
 * @author Carl Hartung (chartung@nafundi.com)
 */
@SuppressLint("ViewConstructor")
public class BearingWidget extends QuestionWidget implements BinaryWidget {
    private Button getBearingButton;
    private TextView answerDisplay;

    public BearingWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        getBearingButton = getSimpleButton(getContext().getString(R.string.get_bearing));
        getBearingButton.setEnabled(!prompt.isReadOnly());
        if (prompt.isReadOnly()) {
            getBearingButton.setVisibility(View.GONE);
        }

        answerDisplay = getCenteredAnswerTextView();

        String s = prompt.getAnswerText();
        if (s != null && !s.equals("")) {
            getBearingButton.setText(getContext().getString(
                    R.string.replace_bearing));
            setBinaryData(s);
        }

        // when you press the button
        getBearingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "recordBearing", "click",
                                formEntryPrompt.getIndex());
                Intent i;
                i = new Intent(getContext(), BearingActivity.class);

                FormController formController = Collect.getInstance().getFormController();
                if (formController != null) {
                    formController.setIndexWaitingForData(formEntryPrompt.getIndex());
                }

                ((Activity) getContext()).startActivityForResult(i,
                        FormEntryActivity.BEARING_CAPTURE);
            }
        });

        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(getBearingButton);
        answerLayout.addView(answerDisplay);
        addAnswerView(answerLayout);
    }

    @Override
    public void clearAnswer() {
        answerDisplay.setText(null);
        getBearingButton.setText(getContext()
                .getString(R.string.get_bearing));
    }

    @Override
    public IAnswerData getAnswer() {
        String s = answerDisplay.getText().toString();
        if (s.equals("")) {
            return null;
        } else {
            return new StringData(s);
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
    public void setBinaryData(Object answer) {
        answerDisplay.setText((String) answer);
        cancelWaitingForBinaryData();
    }

    @Override
    public boolean isWaitingForBinaryData() {
        FormController formController = Collect.getInstance().getFormController();

        return formController != null
                && formEntryPrompt.getIndex().equals(formController.getIndexWaitingForData());

    }

    @Override
    public void cancelWaitingForBinaryData() {
        FormController formController = Collect.getInstance().getFormController();
        if (formController == null) {
            return;
        }

        formController.setIndexWaitingForData(null);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        getBearingButton.setOnLongClickListener(l);
        answerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        getBearingButton.cancelLongPress();
        answerDisplay.cancelLongPress();
    }
}
