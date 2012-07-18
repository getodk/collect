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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Widget that allows user to scan barcodes and add them to the form.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class TriggerWidget extends QuestionWidget {

    private CheckBox mTriggerButton;
    private TextView mStringAnswer;
    private static final String mOK = "OK";

    private FormEntryPrompt mPrompt;


    public FormEntryPrompt getPrompt() {
        return mPrompt;
    }


    public TriggerWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        mPrompt = prompt;

        this.setOrientation(LinearLayout.VERTICAL);

        mTriggerButton = new CheckBox(getContext());
        mTriggerButton.setId(QuestionWidget.newUniqueId());
        mTriggerButton.setText(getContext().getString(R.string.trigger));
        mTriggerButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        // mActionButton.setPadding(20, 20, 20, 20);
        mTriggerButton.setEnabled(!prompt.isReadOnly());

        mTriggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTriggerButton.isChecked()) {
                    mStringAnswer.setText(mOK);
                	Collect.getInstance().getActivityLogger().logInstanceAction(TriggerWidget.this, "triggerButton", 
                			"OK", mPrompt.getIndex());
                } else {
                    mStringAnswer.setText(null);
                	Collect.getInstance().getActivityLogger().logInstanceAction(TriggerWidget.this, "triggerButton", 
                			"null", mPrompt.getIndex());
                }
            }
        });

        mStringAnswer = new TextView(getContext());
        mStringAnswer.setId(QuestionWidget.newUniqueId());
        mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mStringAnswer.setGravity(Gravity.CENTER);

        String s = prompt.getAnswerText();
        if (s != null) {
            if (s.equals(mOK)) {
                mTriggerButton.setChecked(true);
            } else {
                mTriggerButton.setChecked(false);
            }
            mStringAnswer.setText(s);

        }

        // finish complex layout
        this.addView(mTriggerButton);
        // this.addView(mStringAnswer);
    }


    @Override
    public void clearAnswer() {
        mStringAnswer.setText(null);
        mTriggerButton.setChecked(false);
    }


    @Override
    public IAnswerData getAnswer() {
        String s = mStringAnswer.getText().toString();
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
        mTriggerButton.setOnLongClickListener(l);
        mStringAnswer.setOnLongClickListener(l);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mTriggerButton.cancelLongPress();
        mStringAnswer.cancelLongPress();
    }

}
