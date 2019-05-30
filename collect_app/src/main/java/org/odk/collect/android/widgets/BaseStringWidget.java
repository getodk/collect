/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.widgets;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.TableLayout;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.ViewIds;

public abstract class BaseStringWidget extends QuestionWidget {
    protected EditText answerText;

    public BaseStringWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        answerText = new EditText(context);
        answerText.setId(ViewIds.generateViewId());
        answerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());

        // needed to make long read only text scroll
        answerText.setHorizontallyScrolling(false);
        answerText.setSingleLine(false);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        answerText.setLayoutParams(params);

        answerText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                widgetValueChanged();
            }
        });
    }
}
