/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.TypedValue;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.logic.PromptElement;

import java.text.NumberFormat;

/**
 * A widget that restricts values to floating point numbers.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class DecimalWidget extends StringWidget implements IQuestionWidget {

    public DecimalWidget(Context context) {
        super(context);
    }


    public DecimalWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void buildView(PromptElement prompt) {

        // formatting
        setTextSize(TypedValue.COMPLEX_UNIT_PT, GlobalConstants.APPLICATION_FONTSIZE);
        setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // needed to make long readonly text scroll
        setHorizontallyScrolling(false);
        setSingleLine(false);

        // only numbers are allowed
        setKeyListener(new DigitsKeyListener(true, true));

        // only 15 characters allowed
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(15);
        setFilters(fa);
        
        // in case xforms calcuate returns a double, convert to integer
        Double d = (Double) prompt.getAnswerObject();
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(15);
        nf.setMaximumIntegerDigits(15);
        nf.setGroupingUsed(false);
        if (d != null) {
            setText(nf.format(d));
        }

        // disable if read only
        if (prompt.isReadonly()) {
            setBackgroundDrawable(null);
            setFocusable(false);
            setClickable(false);
        }

    }


    @Override
    public IAnswerData getAnswer() {
        String s = getText().toString();
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
