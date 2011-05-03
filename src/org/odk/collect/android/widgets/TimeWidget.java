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

import android.content.Context;
import android.text.format.Time;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Displays a TimePicker widget.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class TimeWidget extends QuestionWidget {

    private TimePicker mTimePicker;
    // Tue May 03 08:49:00 PDT 2011
    private SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
    
    public TimeWidget(Context context, final FormEntryPrompt prompt) {
        super(context, prompt);

        mTimePicker = new TimePicker(getContext());
        mTimePicker.setFocusable(!prompt.isReadOnly());
        mTimePicker.setEnabled(!prompt.isReadOnly());
        mTimePicker.setIs24HourView(true);

        // If there's an answer, use it.
        if (prompt.getAnswerValue() != null) {
            String time = ((TimeData) prompt.getAnswerValue()).getValue().toString();
            try {
                Date d = sdf.parse(time);
                mTimePicker.setCurrentHour(d.getHours());
                mTimePicker.setCurrentMinute(d.getMinutes());
            } catch (ParseException e) {
                // bad date, clear answer
                clearAnswer();
                e.printStackTrace();
            }
            
        } else {
            // create time widget with current time as of right now
            clearAnswer();
        }

        setGravity(Gravity.LEFT);
        addView(mTimePicker);

    }


    /**
     * Resets time to today.
     */
    @Override
    public void clearAnswer() {
        Calendar c = Calendar.getInstance();
        mTimePicker.setCurrentHour(c.get(Calendar.HOUR));
        mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
    }


    @Override
    public IAnswerData getAnswer() {
        Date d = new Date(0);
        d.setHours(mTimePicker.getCurrentHour());
        d.setMinutes(mTimePicker.getCurrentMinute());
        return new TimeData(d);
    }


    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

}
