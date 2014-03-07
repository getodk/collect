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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.BearingActivity;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * BearingWidget is the widget that allows the user to get a compass heading.
 * 
 * @author Carl Hartung (chartung@nafundi.com)
 */
public class BearingWidget extends QuestionWidget implements IBinaryWidget {
    private Button mGetBearingButton;
    private TextView mStringAnswer;
    private TextView mAnswerDisplay;

    public BearingWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        setOrientation(LinearLayout.VERTICAL);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        mGetBearingButton = new Button(getContext());
        mGetBearingButton.setId(QuestionWidget.newUniqueId());
        mGetBearingButton.setPadding(20, 20, 20, 20);
        mGetBearingButton.setText(getContext()
                .getString(R.string.get_bearing));
        mGetBearingButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                mAnswerFontsize);
        mGetBearingButton.setEnabled(!prompt.isReadOnly());
        mGetBearingButton.setLayoutParams(params);
        if (prompt.isReadOnly()) {
            mGetBearingButton.setVisibility(View.GONE);
        }

        mStringAnswer = new TextView(getContext());
        mStringAnswer.setId(QuestionWidget.newUniqueId());

        mAnswerDisplay = new TextView(getContext());
        mAnswerDisplay.setId(QuestionWidget.newUniqueId());
        mAnswerDisplay
                .setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mAnswerDisplay.setGravity(Gravity.CENTER);

        String s = prompt.getAnswerText();
        if (s != null && !s.equals("")) {
            mGetBearingButton.setText(getContext().getString(
                    R.string.replace_bearing));
            setBinaryData(s);
        }

        // when you press the button
        mGetBearingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "recordBearing", "click",
                                mPrompt.getIndex());
                Intent i = null;
                i = new Intent(getContext(), BearingActivity.class);

                Collect.getInstance().getFormController()
                        .setIndexWaitingForData(mPrompt.getIndex());
                ((Activity) getContext()).startActivityForResult(i,
                        FormEntryActivity.BEARING_CAPTURE);
            }
        });

        addView(mGetBearingButton);
        addView(mAnswerDisplay);
    }

    @Override
    public void clearAnswer() {
        mStringAnswer.setText(null);
        mAnswerDisplay.setText(null);
        mGetBearingButton.setText(getContext()
                .getString(R.string.get_bearing));

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
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setBinaryData(Object answer) {
        String s = (String) answer;
        mStringAnswer.setText(s);

        mAnswerDisplay.setText(s);
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public boolean isWaitingForBinaryData() {
        return mPrompt.getIndex().equals(
                Collect.getInstance().getFormController()
                        .getIndexWaitingForData());
    }

    @Override
    public void cancelWaitingForBinaryData() {
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mGetBearingButton.setOnLongClickListener(l);
        mStringAnswer.setOnLongClickListener(l);
        mAnswerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mGetBearingButton.cancelLongPress();
        mStringAnswer.cancelLongPress();
        mAnswerDisplay.cancelLongPress();
    }

}
