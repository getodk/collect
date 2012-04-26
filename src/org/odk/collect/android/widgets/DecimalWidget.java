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

import java.text.NumberFormat;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;

/**
 * A widget that restricts values to floating point numbers.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class DecimalWidget extends StringWidget {

    public DecimalWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        // formatting
        mAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mAnswer.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // needed to make long readonly text scroll
        mAnswer.setHorizontallyScrolling(false);
        mAnswer.setSingleLine(false);

        // only numbers are allowed
        mAnswer.setKeyListener(new DigitsKeyListener(true, true));

        // only 15 characters allowed
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(15);
        mAnswer.setFilters(fa);

        Double d = null;
        if (prompt.getAnswerValue() != null) {
            if (prompt.getAnswerValue().getValue() instanceof Integer){
                d =  new Double(((Integer)prompt.getAnswerValue().getValue()).intValue());
            } else {
                d =  (Double) prompt.getAnswerValue().getValue();
            }
        }

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(15);
        nf.setMaximumIntegerDigits(15);
        nf.setGroupingUsed(false);
        if (d != null) {
            Double dAnswer;
            if (prompt.getAnswerValue().getValue() instanceof Integer){
                dAnswer =  new Double(((Integer)prompt.getAnswerValue().getValue()).intValue());
            } else {
                dAnswer =  (Double) prompt.getAnswerValue().getValue();
            }
            String dString = nf.format(dAnswer);
            d = Double.parseDouble(dString.replace(',', '.'));
            mAnswer.setText(d.toString());
        }

        // disable if read only
        if (prompt.isReadOnly()) {
            setBackgroundDrawable(null);
            setFocusable(false);
            setClickable(false);
        }
    }


    @Override
    public IAnswerData getAnswer() {
        String s = mAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            try {
                return new DecimalData(Double.valueOf(s).doubleValue());
            } catch (Exception NumberFormatException) {
                return null;
            }
        }
    }

}
