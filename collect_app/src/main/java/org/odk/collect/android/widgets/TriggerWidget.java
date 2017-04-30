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

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

/**
 * Widget that allows user to scan barcodes and add them to the form.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class TriggerWidget extends QuestionWidget {

    private CheckBox triggerButton;
    private TextView stringAnswer;
    private static final String mOK = "OK";

    private FormEntryPrompt prompt;


    public FormEntryPrompt getPrompt() {
        return prompt;
    }


    public TriggerWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        this.prompt = prompt;

        triggerButton = new CheckBox(getContext());
        triggerButton.setId(QuestionWidget.newUniqueId());
        triggerButton.setText(getContext().getString(R.string.trigger));
        triggerButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        // mActionButton.setPadding(20, 20, 20, 20);
        triggerButton.setEnabled(!prompt.isReadOnly());

        triggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (triggerButton.isChecked()) {
                    stringAnswer.setText(mOK);
                    Collect.getInstance().getActivityLogger().logInstanceAction(TriggerWidget.this,
                            "triggerButton",
                            "OK", TriggerWidget.this.prompt.getIndex());
                } else {
                    stringAnswer.setText(null);
                    Collect.getInstance().getActivityLogger().logInstanceAction(TriggerWidget.this,
                            "triggerButton",
                            "null", TriggerWidget.this.prompt.getIndex());
                }
            }
        });

        stringAnswer = new TextView(getContext());
        stringAnswer.setId(QuestionWidget.newUniqueId());
        stringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        stringAnswer.setGravity(Gravity.CENTER);

        String s = prompt.getAnswerText();
        if (s != null) {
            if (s.equals(mOK)) {
                triggerButton.setChecked(true);
            } else {
                triggerButton.setChecked(false);
            }
            stringAnswer.setText(s);

        }

        // finish complex layout
        addAnswerView(triggerButton);
    }


    @Override
    public void clearAnswer() {
        stringAnswer.setText(null);
        triggerButton.setChecked(false);
    }


    @Override
    public IAnswerData getAnswer() {
        String s = stringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
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
        triggerButton.setOnLongClickListener(l);
        stringAnswer.setOnLongClickListener(l);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        triggerButton.cancelLongPress();
        stringAnswer.cancelLongPress();
    }

}
