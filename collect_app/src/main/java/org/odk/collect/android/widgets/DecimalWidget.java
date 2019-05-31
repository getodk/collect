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
import androidx.annotation.NonNull;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.method.DigitsKeyListener;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.ThousandsSeparatorTextWatcher;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * A widget that restricts values to floating point numbers.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class DecimalWidget extends StringWidget {

    boolean useThousandSeparator;

    public DecimalWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride, boolean useThousandSeparator) {
        super(context, prompt, readOnlyOverride);

        answerText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        answerText.setKeyListener(new DigitsKeyListener(true, true));

        this.useThousandSeparator = useThousandSeparator;
        if (useThousandSeparator) {
            answerText.addTextChangedListener(new ThousandsSeparatorTextWatcher(answerText));
        }

        setUpInputFilter();
    }

    private void setUpInputFilter() {
        // only 15 characters allowed
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(15);
        if (useThousandSeparator) {
            fa[0] = new InputFilter.LengthFilter(19);
        }
        answerText.setFilters(fa);
    }

    private Double getDoubleAnswerValue() {
        IAnswerData dataHolder = getFormEntryPrompt().getAnswerValue();
        Double d = null;
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue != null) {
                d = dataValue instanceof Integer ? (double) (Integer) dataValue : (Double) dataValue;
            }
        }
        return d;
    }

    @Override
    public void setDisplayValueFromModel() {
        Double d = getDoubleAnswerValue();

        if (d != null) {
            // truncate to 15 digits max in US locale
            // use US locale because DigitsKeyListener can't be localized before API 26
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMaximumFractionDigits(15);
            nf.setMaximumIntegerDigits(15);
            nf.setGroupingUsed(false);

            answerText.setText(nf.format(d));
            Selection.setSelection(answerText.getText(), answerText.getText().length());
        }
    }

    @NonNull
    @Override
    public String getAnswerText() {
        return useThousandSeparator
                ? ThousandsSeparatorTextWatcher.getOriginalString(super.getAnswerText())
                : super.getAnswerText();
    }

    @Override
    public IAnswerData getAnswer() {
        String s = answerText.getText().toString();
        if (useThousandSeparator) {
            s = ThousandsSeparatorTextWatcher.getOriginalString(s);
        }

        if (s.isEmpty()) {
            return null;
        } else {
            try {
                return new DecimalData(Double.parseDouble(s));
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

}
