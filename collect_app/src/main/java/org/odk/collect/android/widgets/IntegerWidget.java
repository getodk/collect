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
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.widget.EditText;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.ThousandsSeparatorTextWatcher;

import java.util.Locale;

/**
 * Widget that restricts values to integers.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class IntegerWidget extends StringWidget {

    boolean useThousandSeparator;

    public IntegerWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride, boolean useThousandSeparator) {
        super(context, prompt, readOnlyOverride);

        EditText answerText = getAnswerTextField();
        answerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        answerText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);

        // needed to make long readonly text scroll
        answerText.setHorizontallyScrolling(false);
        answerText.setSingleLine(false);

        this.useThousandSeparator = useThousandSeparator;
        if (useThousandSeparator) {
            answerText.addTextChangedListener(new ThousandsSeparatorTextWatcher(answerText));
        }

        // only allows numbers and no periods
        answerText.setKeyListener(new DigitsKeyListener(true, false));

        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        if (useThousandSeparator) {
            //11 since for a nine digit number , their will be 2 separators.
            fa[0] = new InputFilter.LengthFilter(11);
        }
        answerText.setFilters(fa);

        if (prompt.isReadOnly()) {
            setBackground(null);
            setFocusable(false);
            setClickable(false);
        }

        Integer i = getIntegerAnswerValue();

        if (i != null) {
            answerText.setText(String.format(Locale.US, "%d", i));
            Selection.setSelection(answerText.getText(), answerText.getText().toString().length());
        }
    }

    private Integer getIntegerAnswerValue() {
        IAnswerData dataHolder = getFormEntryPrompt().getAnswerValue();
        Integer d = null;
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue != null) {
                if (dataValue instanceof Double) {
                    d = ((Double) dataValue).intValue();
                } else {
                    d = (Integer) dataValue;
                }
            }
        }
        return d;
    }

    @NonNull
    @Override
    public String getAnswerText() {
        if (useThousandSeparator) {
            return ThousandsSeparatorTextWatcher.getOriginalString(super.getAnswerText());
        }
        return super.getAnswerText();
    }

    @Override
    public IAnswerData getAnswer() {
        clearFocus();
        String s = getAnswerTextField().getText().toString();
        if (useThousandSeparator) {
            s = ThousandsSeparatorTextWatcher.getOriginalString(s);
        }

        if (s.isEmpty()) {
            return null;

        } else {
            try {
                return new IntegerData(Integer.parseInt(s));
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

}
