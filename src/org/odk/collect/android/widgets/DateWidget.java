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

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class DateWidget extends AbstractQuestionWidget {
    
    private DatePicker mDatePicker;
    private DatePicker.OnDateChangedListener mDateListener;

    // convert from j2me date to android date
    private final static int YEARSHIFT = 1900;


    public DateWidget(Handler handler, Context context, FormEntryPrompt prompt) {
        super(handler, context, prompt);
    }

    @Override
	public IAnswerData getAnswer() {
        // clear focus first so the datewidget gets the value in the text box
        mDatePicker.clearFocus();
        Date d =
            new Date(mDatePicker.getYear() - YEARSHIFT, mDatePicker.getMonth(), mDatePicker
                    .getDayOfMonth());
        return new DateData(d);
    }

    /**
     * Build view for date answer. Includes retrieving existing answer.
     */
    @Override
    protected void buildViewBodyImpl() {
        final Calendar c = new GregorianCalendar();
        
        mDatePicker = new DatePicker(getContext());
        mDatePicker.setFocusable(!prompt.isReadOnly());
        mDatePicker.setEnabled(!prompt.isReadOnly());

        mDateListener = new DatePicker.OnDateChangedListener() {
            @Override
			public void onDateChanged(DatePicker view, int year, int month, int day) {
            	// TODO: MES -- is this broken if calculated date?
            	// TODO: MES -- Or if calculated readonly() field?
            	signalDescendant(true);
                if (!prompt.isReadOnly()) {
                    // handle leap years and number of days in month
                    // TODO
                    // http://code.google.com/p/android/issues/detail?id=2081
                    c.set(year, month, 1);
                    int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (day > max) {
                        view.updateDate(year, month, max);
                    } else {
                        view.updateDate(year, month, day);
                    }
                }
            }
        };

        setGravity(Gravity.LEFT);
        addView(mDatePicker);
    }

    protected void updateViewAfterAnswer() {
    	IAnswerData answer = prompt.getAnswerValue();
    	if ( answer == null ) {
            final Calendar c = new GregorianCalendar();
            mDatePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                mDateListener);
    	} else {
            Date d = (Date) prompt.getAnswerValue().getValue();
            mDatePicker.init(d.getYear() + YEARSHIFT, d.getMonth(), d.getDate(), mDateListener);
    	}
    }
    
    @Override
	public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setEnabled(boolean isEnabled) {
    	mDatePicker.setEnabled(isEnabled && !prompt.isReadOnly());
    }
}
