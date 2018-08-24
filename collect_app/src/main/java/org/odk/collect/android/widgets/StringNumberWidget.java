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

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.InputType;
import android.text.Selection;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.widget.EditText;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.ThousandsSeparatorTextWatcher;

/**
 * Widget that restricts values to integers.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class StringNumberWidget extends StringWidget {

    boolean useThousandSeparator;

    public StringNumberWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride, boolean useThousandSeparator) {
        super(context, prompt, readOnlyOverride);

        EditText answerTextField = getAnswerTextField();

        answerTextField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        answerTextField.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);

        // needed to make long readonly text scroll
        answerTextField.setHorizontallyScrolling(false);
        answerTextField.setSingleLine(false);

        this.useThousandSeparator = useThousandSeparator;
        if (useThousandSeparator) {
            answerTextField.addTextChangedListener(new ThousandsSeparatorTextWatcher(answerTextField));
        }

        answerTextField.setKeyListener(new DigitsKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return new char[]{
                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-', '+', ' ', ','
                };
            }
        });

        if (prompt.isReadOnly()) {
            setBackground(null);
            setFocusable(false);
            setClickable(false);
        }

        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = (String) prompt.getAnswerValue().getValue();
        }

        if (s != null) {
            answerTextField.setText(s);
            Selection.setSelection(answerTextField.getText(), answerTextField.getText().toString().length());
        }
    }

    @Override
    public IAnswerData getAnswer() {
        clearFocus();
        String s = getAnswerText();

        if (useThousandSeparator) {
            s = ThousandsSeparatorTextWatcher.getOriginalString(s);
        }

        if (s.isEmpty()) {
            return null;
        } else {
            try {
                return new StringData(s);
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

}
