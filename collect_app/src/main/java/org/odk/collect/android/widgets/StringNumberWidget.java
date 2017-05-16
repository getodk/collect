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
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;

import org.javarosa.form.api.FormEntryPrompt;

import java.math.BigDecimal;

/**
 * Widget that restricts values to decimal numbers.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class StringNumberWidget extends StringWidget {

    public StringNumberWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride,
                              BigDecimal rangeStart, BigDecimal rangeEnd, BigDecimal rangeStep
                              /* ToDo adapt this, or another widget to handle the range */) {
        super(context, prompt, readOnlyOverride, true);

        mAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mAnswer.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);

        // needed to make long readonly text scroll
        mAnswer.setHorizontallyScrolling(false);
        mAnswer.setSingleLine(false);

        mAnswer.setKeyListener(new DigitsKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return "0123456789.-+ ,".toCharArray(); // ToDo consider disallowing comma, since they could be put in the wrong places
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
            mAnswer.setText(s);
        }

        setupChangeListener();
    }
}
