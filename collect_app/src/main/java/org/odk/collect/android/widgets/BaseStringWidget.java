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
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TableLayout;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.utilities.ViewIds;

public abstract class BaseStringWidget extends QuestionWidget {
    protected EditText answerText;

    public BaseStringWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        setUpAnswerText();
        setDisplayValueFromModel();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return !event.isAltPressed() && super.onKeyDown(keyCode, event);
    }

    @Override
    public void clearAnswer() {
        answerText.setText(null);
    }

    @Override
    public IAnswerData getAnswer() {
        String s = getAnswerText();
        return !s.equals("") ? new StringData(s) : null;
    }

    @NonNull
    public String getAnswerText() {
        return answerText.getText().toString();
    }

    /**
     * Registers all subviews except for the EditText to clear on long press. This makes it possible
     * to long-press to paste or perform other text editing functions.
     */
    @Override
    protected void registerToClearAnswerOnLongPress(FormEntryActivity activity) {
        for (int i = 0; i < getChildCount(); i++) {
            if (!(getChildAt(i) instanceof EditText)) {
                activity.registerForContextMenu(getChildAt(i));
            }
        }
    }

    public abstract void setDisplayValueFromModel();

    private void setUpAnswerText() {
        answerText = new EditText(getContext());
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

    protected void setUpIntegerInputFilter(boolean useThousandSeparator) {
        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        if (useThousandSeparator) {
            //11 since for a nine digit number , their will be 2 separators.
            fa[0] = new InputFilter.LengthFilter(11);
        }
        answerText.setFilters(fa);
    }

    protected void setUpDecimalInputFilter(boolean useThousandSeparator) {
        // only 15 characters allowed
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(15);
        if (useThousandSeparator) {
            fa[0] = new InputFilter.LengthFilter(19);
        }
        answerText.setFilters(fa);
    }
}
