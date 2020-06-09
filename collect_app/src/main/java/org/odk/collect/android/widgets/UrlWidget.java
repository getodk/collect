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

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.CustomTabHelper;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.widgets.interfaces.ButtonWidget;

/**
 * Widget that allows user to open URLs from within the form
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class UrlWidget extends QuestionWidget implements ButtonWidget {

    private final CustomTabHelper customTabHelper;
    private Uri uri;

    protected Button openUrlButton;
    protected TextView stringAnswer;

    public UrlWidget(Context context, QuestionDetails questionDetails, CustomTabHelper customTabHelper) {
        super(context, questionDetails);
        this.customTabHelper = customTabHelper;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        ViewGroup answerView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.url_widget_answer, null);

        openUrlButton = answerView.findViewById(R.id.url_button);
        stringAnswer = answerView.findViewById(R.id.url_answer_text);

        if (prompt.isReadOnly()) {
            openUrlButton.setVisibility(GONE);
        } else {
            openUrlButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            openUrlButton.setOnClickListener(v -> {
                if (MultiClickGuard.allowClick(QuestionWidget.class.getName())) {
                    onButtonClick(openUrlButton.getId());
                }
            });
        }

        stringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        String answerText = prompt.getAnswerText();
        if (answerText != null) {
            stringAnswer.setText(answerText);
            uri = Uri.parse(stringAnswer.getText().toString());
        }

        return answerView;
    }

    private boolean isUrlEmpty(TextView stringAnswer) {
        return stringAnswer == null || stringAnswer.getText() == null
                || stringAnswer.getText().toString().isEmpty();
    }

    @Override
    public void clearAnswer() {
        Toast.makeText(getContext(), "URL is readonly", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IAnswerData getAnswer() {
        String answerText = stringAnswer.getText().toString();
        return !answerText.isEmpty()
                ? new StringData(answerText)
                : null;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        openUrlButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        openUrlButton.cancelLongPress();
        stringAnswer.cancelLongPress();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (customTabHelper.getServiceConnection() != null) {
            getContext().unbindService(customTabHelper.getServiceConnection());
        }
    }

    @Override
    public void onButtonClick(int buttonId) {
        if (!isUrlEmpty(stringAnswer)) {
            customTabHelper.bindCustomTabsService(getContext(), null);
            customTabHelper.openUri(getContext(), uri);
        } else {
            Toast.makeText(getContext(), "No URL set", Toast.LENGTH_SHORT).show();
        }
    }
}
