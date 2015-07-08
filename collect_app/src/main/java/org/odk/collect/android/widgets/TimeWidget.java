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
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.odk.collect.android.application.Collect;

import android.content.Context;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.TimePicker;

import java.util.Date;

/**
 * Displays a TimePicker widget.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class TimeWidget extends QuestionWidget {

    private TimePicker mTimePicker;


    public TimeWidget(Context context, final FormEntryPrompt prompt) {
        super(context, prompt);

        mTimePicker = new TimePicker(getContext());
        mTimePicker.setId(QuestionWidget.newUniqueId());
        mTimePicker.setFocusable(!prompt.isReadOnly());
        mTimePicker.setEnabled(!prompt.isReadOnly());

        String clockType =
            android.provider.Settings.System.getString(context.getContentResolver(),
                android.provider.Settings.System.TIME_12_24);
        if (clockType == null || clockType.equalsIgnoreCase("24")) {
            mTimePicker.setIs24HourView(true);
        }

        // If there's an answer, use it.
        if (prompt.getAnswerValue() != null) {

            // create a new date time from date object using default time zone
            DateTime ldt =
                new DateTime(((Date) ((TimeData) prompt.getAnswerValue()).getValue()).getTime());
            System.out.println("retrieving:" + ldt);

            mTimePicker.setCurrentHour(ldt.getHourOfDay());
            mTimePicker.setCurrentMinute(ldt.getMinuteOfHour());

        } else {
            // create time widget with current time as of right now
            clearAnswer();
        }

        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            	Collect.getInstance().getActivityLogger().logInstanceAction(TimeWidget.this, "onTimeChanged", 
            			String.format("%1$02d:%2$02d",hourOfDay, minute), mPrompt.getIndex());
			}
		});

        setGravity(Gravity.LEFT);
        addView(mTimePicker);

    }


    /**
     * Resets time to today.
     */
    @Override
    public void clearAnswer() {
        DateTime ldt = new DateTime();
        mTimePicker.setCurrentHour(ldt.getHourOfDay());
        mTimePicker.setCurrentMinute(ldt.getMinuteOfHour());
    }


    @Override
    public IAnswerData getAnswer() {
    	clearFocus();
        // use picker time, convert to today's date, store as utc
        DateTime ldt =
            (new DateTime()).withTime(mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute(),
                0, 0);
        //DateTime utc = ldt.withZone(DateTimeZone.forID("UTC"));
        System.out.println("storing:" + ldt);
        return new TimeData(ldt.toDate());
    }


    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }


    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mTimePicker.setOnLongClickListener(l);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mTimePicker.cancelLongPress();
    }

}
