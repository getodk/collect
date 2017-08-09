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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.BearingActivity;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;

/**
 * BearingWidget is the widget that allows the user to get a compass heading.
 *
 * @author Carl Hartung (chartung@nafundi.com)
 */
public class BearingWidget extends QuestionWidget implements IBinaryWidget {
    private Button getBearingButton;
    private TextView stringAnswer;
    private TextView answerDisplay;

    public BearingWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        getBearingButton = new Button(getContext());
        getBearingButton.setId(QuestionWidget.newUniqueId());
        getBearingButton.setPadding(20, 20, 20, 20);
        getBearingButton.setText(getContext()
                .getString(R.string.get_bearing));
        getBearingButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                answerFontsize);
        getBearingButton.setEnabled(!prompt.isReadOnly());
        getBearingButton.setLayoutParams(params);
        if (prompt.isReadOnly()) {
            getBearingButton.setVisibility(View.GONE);
        }

        stringAnswer = new TextView(getContext());
        stringAnswer.setId(QuestionWidget.newUniqueId());

        answerDisplay = new TextView(getContext());
        answerDisplay.setId(QuestionWidget.newUniqueId());
        answerDisplay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        answerDisplay.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor));
        answerDisplay.setGravity(Gravity.CENTER);

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
                Intent i = null;
                i = new Intent(getContext(), BearingActivity.class);

                Collect.getInstance().getFormController()
                        .setIndexWaitingForData(formEntryPrompt.getIndex());
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
        stringAnswer.setText(null);
        answerDisplay.setText(null);
        getBearingButton.setText(getContext()
                .getString(R.string.get_bearing));

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
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setBinaryData(Object answer) {
        String s = (String) answer;
        stringAnswer.setText(s);

        answerDisplay.setText(s);
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public boolean isWaitingForBinaryData() {
        return formEntryPrompt.getIndex().equals(
                Collect.getInstance().getFormController()
                        .getIndexWaitingForData());
    }

    @Override
    public void cancelWaitingForBinaryData() {
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        getBearingButton.setOnLongClickListener(l);
        stringAnswer.setOnLongClickListener(l);
        answerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        getBearingButton.cancelLongPress();
        stringAnswer.cancelLongPress();
        answerDisplay.cancelLongPress();
    }

}
