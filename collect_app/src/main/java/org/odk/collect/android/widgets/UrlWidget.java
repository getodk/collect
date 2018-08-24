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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.CustomTabHelper;
import org.odk.collect.android.widgets.interfaces.ButtonWidget;

/**
 * Widget that allows user to open URLs from within the form
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class UrlWidget extends QuestionWidget implements ButtonWidget {

    private Uri uri;
    private final Button openUrlButton;
    private final TextView stringAnswer;
    private final CustomTabHelper customTabHelper;

    public UrlWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        openUrlButton = getSimpleButton(context.getString(R.string.open_url));
        openUrlButton.setEnabled(!prompt.isReadOnly());

        stringAnswer = getCenteredAnswerTextView();

        String s = prompt.getAnswerText();
        if (s != null) {
            stringAnswer.setText(s);
            uri = Uri.parse(stringAnswer.getText().toString());
        }

        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(openUrlButton);
        answerLayout.addView(stringAnswer);
        addAnswerView(answerLayout);

        customTabHelper = new CustomTabHelper();
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
        String s = stringAnswer.getText().toString();
        return !s.isEmpty()
                ? new StringData(s)
                : null;
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

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (customTabHelper.getServiceConnection() != null) {
            getContext().unbindService(customTabHelper.getServiceConnection());
        }
    }

    @Override
    public void onButtonClick(int buttonId) {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "openUrl", "click",
                        getFormEntryPrompt().getIndex());

        if (!isUrlEmpty(stringAnswer)) {
            customTabHelper.bindCustomTabsService(getContext(), null);
            customTabHelper.openUri(getContext(), uri);
        } else {
            Toast.makeText(getContext(), "No URL set", Toast.LENGTH_SHORT).show();
        }
    }
}
