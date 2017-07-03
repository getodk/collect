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
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.CustomTabHelper;

/**
 * Widget that allows user to open URLs from within the form
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class UrlWidget extends QuestionWidget {
    private CustomTabHelper customTabHelper;
    private Uri uri;

    private Button openUrlButton;
    private TextView stringAnswer;

    public UrlWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        View answerLayout = inflate(context, R.layout.url_widget_layout, null);

        // set button formatting
        openUrlButton = (Button) answerLayout.findViewById(R.id.openUrl);
        openUrlButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        openUrlButton.setEnabled(!prompt.isReadOnly());

        openUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "openUrl", "click",
                                formEntryPrompt.getIndex());

                if (stringAnswer != null & stringAnswer.getText() != null
                        && !"".equalsIgnoreCase((String) stringAnswer.getText())) {

                    openUrl();
                } else {
                    Toast.makeText(getContext(), "No URL set", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // set text formatting
        stringAnswer = (TextView) answerLayout.findViewById(R.id.url);
        stringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);

        String s = prompt.getAnswerText();
        if (s != null) {
            stringAnswer.setText(s);
            uri = Uri.parse(stringAnswer.getText().toString());
        }

        // finish complex layout
        addAnswerView(answerLayout);

        customTabHelper = new CustomTabHelper();
        customTabHelper.bindCustomTabsService((AppCompatActivity) context, null);
    }

    private void openUrl() {
        if (customTabHelper.getPackageName(getContext()).size() != 0) {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
            customTabsIntent.intent.setPackage(customTabHelper.getPackageName(getContext()).get(0));
            customTabsIntent.launchUrl(getContext(), uri);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            getContext().startActivity(intent);
        }
    }

    @Override
    public void clearAnswer() {
        Toast.makeText(getContext(), "URL is readonly", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IAnswerData getAnswer() {
        String s = stringAnswer.getText().toString();
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
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        openUrlButton.cancelLongPress();
        stringAnswer.cancelLongPress();
    }

}
