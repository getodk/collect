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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.odk.collect.android.PromptElement;
import org.odk.collect.android.SharedConstants;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.TypedValue;

/**
 * Widget that restricts values to integers.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class IntegerWidget extends StringWidget {

    public IntegerWidget(Context context) {
        super(context);
    }


    public IntegerWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void buildView(PromptElement prompt) {

        Integer i = (Integer) prompt.getAnswerObject();
        if (i != null) {
            this.setText(i.toString());
        }
        if (prompt.isReadonly()) {
            this.setBackgroundDrawable(null);
            this.setFocusable(false);
            this.setClickable(false);
        }

        this.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        this.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);

        // needed to make long readonly text scroll
        this.setHorizontallyScrolling(false);
        this.setSingleLine(false);

        this.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);

        // only allows numbers and no periods
        this.setKeyListener(new DigitsKeyListener(true, false));

        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        setFilters(fa);
    }


    @Override
    public IAnswerData getAnswer() {
        String s = this.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            try {
                return new IntegerData(Integer.parseInt(s));
            } catch (Exception NumberFormatException) {
                return null;
            }
        }

    }
}
