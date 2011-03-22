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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.widgets.AbstractQuestionWidget.OnDescendantRequestFocusChangeListener.FocusChangeState;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.widget.TimePicker;

/**
 * Displays a Timepicker widget. TimeWidget 
 * 
 * @author Aurelio Di Pasquale aurdipas@gmail.com
 */
public class TimeWidget extends AbstractQuestionWidget {
    
    private TimePicker mTimePicker;

    public TimeWidget(Handler handler, Context context, FormEntryPrompt prompt) {
        super(handler, context, prompt);
    }

    @Override
	public IAnswerData getAnswer() {
        // clear focus first so the timewidget gets the value in the text box
        mTimePicker.clearFocus();
        Date d = new Date(0);
        d.setHours(mTimePicker.getCurrentHour());
        d.setMinutes(mTimePicker.getCurrentMinute());
        return new TimeData(d);
    }

    /**
     * Build view for time answer. Includes retrieving existing answer.
     */
    @Override
    protected void buildViewBodyImpl() {
        mTimePicker = new TimePicker(getContext());
        mTimePicker.setFocusable(!prompt.isReadOnly());
        mTimePicker.setEnabled(!prompt.isReadOnly());
        mTimePicker.setIs24HourView(true);
        new TimePicker.OnTimeChangedListener() {
             @Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (!prompt.isReadOnly()) {
                    // http://code.google.com/p/android/issues/detail?id=2081
                	view.setCurrentHour(hourOfDay);
                	view.setCurrentMinute(minute);
            }
                // gain focus after change because we might have a 
                // constraint violation somewhere else that will
                // restore focus elsewhere
            	signalDescendant(FocusChangeState.DIVERGE_VIEW_FROM_MODEL);
            }
        };

        setGravity(Gravity.LEFT);
        addView(mTimePicker);
    }

    protected void updateViewAfterAnswer() {
    	IAnswerData answer = prompt.getAnswerValue();
    	if ( answer == null ) {
            final Calendar c = new GregorianCalendar();
            mTimePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
    	} else {
            final Calendar c = new GregorianCalendar();
            c.setTime((Date) prompt.getAnswerValue().getValue());
            mTimePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
    	}
    }

    @Override
    public void setEnabled(boolean isEnabled) {
    	mTimePicker.setEnabled(isEnabled && !prompt.isReadOnly());
    }

}
