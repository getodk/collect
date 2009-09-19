/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.logic.PromptElement;


/**
 * Widget that allows user to scan barcodes and add them to the form.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class TriggerWidget extends LinearLayout implements IQuestionWidget {

    private ToggleButton mActionButton;
    private TextView mStringAnswer;
    private TextView mDisplayText;
    private String yes = "yes";
    private String no = "no";


    public TriggerWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        mStringAnswer.setText(null);
        mActionButton.setChecked(false);
    }


    public IAnswerData getAnswer() {
        String s = mStringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }


    public void buildView(PromptElement prompt) {
        this.setOrientation(LinearLayout.VERTICAL);

        mActionButton = new ToggleButton(getContext());
        mActionButton.setText(getContext().getString(R.string.ack));
        mActionButton.setTextOff(getContext().getString(R.string.ack));
        mActionButton.setTextOn(getContext().getString(R.string.acked));
        mActionButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, GlobalConstants.APPLICATION_FONTSIZE);
        mActionButton.setPadding(20, 20, 20, 20);
        mActionButton.setEnabled(!prompt.isReadonly());

        mActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TriggerWidget.this.mActionButton.isChecked()) {
                    TriggerWidget.this.mStringAnswer.setText(yes);
                } else {
                    TriggerWidget.this.mStringAnswer.setText(no);
                }
            }
        });

        mStringAnswer = new TextView(getContext());
        mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_PT, GlobalConstants.APPLICATION_FONTSIZE);
        mStringAnswer.setGravity(Gravity.CENTER);

        mDisplayText = new TextView(getContext());
        mDisplayText.setPadding(5, 0, 0, 0);

        String s = prompt.getAnswerText();
        if (s != null) {
            if (s.equals(yes)) {
                mActionButton.setChecked(true);
            } else {
                mActionButton.setChecked(false);
            }
            mStringAnswer.setText(s);

        }

        // finish complex layout
        this.addView(mActionButton);
        // this.addView(mStringAnswer);
    }


    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);

    }
}
