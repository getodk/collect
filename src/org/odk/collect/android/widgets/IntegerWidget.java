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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;

/**
 * Widget that restricts values to integers.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class IntegerWidget extends StringWidget {

	private Integer getIntegerAnswerValue() {
		IAnswerData dataHolder = mPrompt.getAnswerValue();
		Integer d = null;
        if (dataHolder != null) {
        	Object dataValue = dataHolder.getValue();
        	if ( dataValue != null ) {
        		if (dataValue instanceof Double){
	                d =  Integer.valueOf(((Double) dataValue).intValue());
	            } else {
	                d =  (Integer)dataValue;
	            }
        	}
        }
        return d;
	}

    public IntegerWidget(Context context, FormEntryPrompt prompt, boolean readOnlyOverride) {
        super(context, prompt, readOnlyOverride, true);

        mAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mAnswer.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);

        // needed to make long readonly text scroll
        mAnswer.setHorizontallyScrolling(false);
        mAnswer.setSingleLine(false);

        // only allows numbers and no periods
        mAnswer.setKeyListener(new DigitsKeyListener(true, false));

        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        mAnswer.setFilters(fa);

        if (prompt.isReadOnly()) {
            setBackgroundDrawable(null);
            setFocusable(false);
            setClickable(false);
        }

        Integer i = getIntegerAnswerValue();

        if (i != null) {
            mAnswer.setText(i.toString());
        }
        
        setupChangeListener();
    }


    @Override
    public IAnswerData getAnswer() {
    	clearFocus();
    	String s = mAnswer.getText().toString();
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
