/*
 * Copyright (C) 2013 Nafundi LLC
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
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

/**
 * Widget that allows user to open URLs from within the form
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class UrlWidget extends QuestionWidget {
    private Button openUrlButton;
    private TextView stringAnswer;

    public UrlWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        // set button formatting
        openUrlButton = new Button(getContext());
        openUrlButton.setId(QuestionWidget.newUniqueId());
        openUrlButton.setText(getContext().getString(R.string.open_url));
        openUrlButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                answerFontsize);
        openUrlButton.setPadding(20, 20, 20, 20);
        openUrlButton.setEnabled(!prompt.isReadOnly());
        openUrlButton.setLayoutParams(params);

        openUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "openUrl", "click",
                                formEntryPrompt.getIndex());

                if (stringAnswer != null & stringAnswer.getText() != null
                        && !"".equalsIgnoreCase((String) stringAnswer.getText())) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse((String) stringAnswer.getText()));
                    getContext().startActivity(i);
                } else {
                    Toast.makeText(getContext(), "No URL set", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // set text formatting
        stringAnswer = new TextView(getContext());
        stringAnswer.setId(QuestionWidget.newUniqueId());
        stringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        stringAnswer.setGravity(Gravity.CENTER);

        String s = prompt.getAnswerText();
        if (s != null) {
            stringAnswer.setText(s);
        }
        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(openUrlButton);
        answerLayout.addView(stringAnswer);
        addAnswerView(answerLayout);
    }

    @Override
    public void clearAnswer() {
        Toast.makeText(getContext(), "URL is readonly", Toast.LENGTH_SHORT).show();
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
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        openUrlButton.cancelLongPress();
        stringAnswer.cancelLongPress();
    }

}
