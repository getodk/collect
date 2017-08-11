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

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * A widget that restricts values to floating point numbers.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class DecimalWidget extends StringWidget {

    private Double getDoubleAnswerValue() {
        IAnswerData dataHolder = formEntryPrompt.getAnswerValue();
        Double d = null;
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue != null) {
                if (dataValue instanceof Integer) {
                    d = (double) (Integer) dataValue;
                } else {
                    d = (Double) dataValue;
                }
            }
        }
        return d;
    }

    public DecimalWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride) {
        super(context, prompt, readOnlyOverride, true);

        // formatting
        answer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        answer.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // needed to make long readonly text scroll
        answer.setHorizontallyScrolling(false);
        answer.setSingleLine(false);

        // only numbers are allowed
        answer.setKeyListener(new DigitsKeyListener(true, true));

        // only 15 characters allowed
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(15);
        answer.setFilters(fa);

        Double d = getDoubleAnswerValue();

        if (d != null) {
            // truncate to 15 digits max in US locale
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMaximumFractionDigits(15);
            nf.setMaximumIntegerDigits(15);
            nf.setGroupingUsed(false);

            String formattedValue = nf.format(d);
            answer.setText(formattedValue);

            Selection.setSelection(answer.getText(), answer.getText().length());
        }

        // disable if read only
        if (prompt.isReadOnly()) {
            setBackground(null);
            setFocusable(false);
            setClickable(false);
        }

        setupChangeListener();
    }


    @Override
    public IAnswerData getAnswer() {
        clearFocus();
        String s = answer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            try {
                return new DecimalData(Double.valueOf(s).doubleValue());
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

}
