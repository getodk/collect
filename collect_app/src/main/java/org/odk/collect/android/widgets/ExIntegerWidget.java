/*
 * Copyright (C) 2012 University of Washington
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

import org.odk.collect.android.external.ExternalAppsUtils;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;


/**
 * Launch an external app to supply an integer value. If the app
 * does not launch, enable the text area for regular data entry.
 *
 * See {@link org.odk.collect.android.widgets.ExStringWidget} for usage.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class ExIntegerWidget extends ExStringWidget {

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

    public ExIntegerWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        mAnswer.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);

        // only allows numbers and no periods
        mAnswer.setKeyListener(new DigitsKeyListener(true, false));

        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        mAnswer.setFilters(fa);

        Integer i = getIntegerAnswerValue();

        if (i != null) {
            mAnswer.setText(i.toString());
        }
    }


    @Override
    protected void fireActivity(Intent i) throws ActivityNotFoundException {
    	i.putExtra("value", getIntegerAnswerValue());
       	Collect.getInstance().getActivityLogger().logInstanceAction(this, "launchIntent",
    			i.getAction(), mPrompt.getIndex());
        ((Activity) getContext()).startActivityForResult(i,
                FormEntryActivity.EX_INT_CAPTURE);
    }


    @Override
    public IAnswerData getAnswer() {
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


    /**
     * Allows answer to be set externally in {@Link FormEntryActivity}.
     */
    @Override
    public void setBinaryData(Object answer) {
        IntegerData integerData = ExternalAppsUtils.asIntegerData(answer);
    	mAnswer.setText( integerData == null ? null : integerData.getValue().toString());
    	Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

}
