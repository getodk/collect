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

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;

import java.text.NumberFormat;

/**
 * A widget that restricts values to floating point numbers.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class DecimalWidget extends StringWidget {

    public DecimalWidget(Handler handler, Context context, FormEntryPrompt prompt) {
        super(handler, context, prompt);
    }

    /**
     * Override this as needed for derived classes
     * 
     * @return the prompt's Answer value as a string
     */
    protected String accessPromptAnswerAsString() {
        String value = null;
        Double d = null;
        if (prompt.getAnswerValue() != null)
            d = (Double) prompt.getAnswerValue().getValue();

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(15);
        nf.setMaximumIntegerDigits(15);
        nf.setGroupingUsed(false);
        if (d != null) {
        	value = nf.format(d);
        }

        return value;
    }

    @Override
    protected void buildViewBodyImpl() {

        // only 15 characters allowed
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(15);

    	// restrict field to only numbers...
        commonBuildView(InputType.TYPE_CLASS_NUMBER | 
   			 InputType.TYPE_NUMBER_FLAG_SIGNED | 
			 InputType.TYPE_NUMBER_FLAG_DECIMAL, fa);
    }


    @Override
    public IAnswerData getAnswer() {
        String s = mStringAnswer.getText().toString();
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
