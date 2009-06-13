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

package org.google.android.odk.widgets;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.google.android.odk.PromptElement;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;

import android.content.Context;
import android.view.Gravity;
import android.widget.DatePicker;
import android.widget.LinearLayout;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not
 * allow dates that do not exist.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class DateWidget extends LinearLayout implements IQuestionWidget {

	private DatePicker mDatePicker;
    private DatePicker.OnDateChangedListener mDateListener;

    // convert from j2me date to android date
    private final static int YEARSHIFT = 1900;


    public DateWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        final Calendar c = new GregorianCalendar();
        mDatePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                mDateListener);
    }


    public IAnswerData getAnswer() {
        Date d = new Date(mDatePicker.getYear() - YEARSHIFT, mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
        return new DateData(d);
    }


    /**
     * Build view for date answer. Includes retrieving existing answer.
     */
    public void buildView(final PromptElement prompt) {
        final Calendar c = new GregorianCalendar();
        
        mDatePicker = new DatePicker(getContext());
        mDateListener = new DatePicker.OnDateChangedListener() {
            public void onDateChanged(DatePicker view, int year, int month, int day) {
                if (prompt.isReadonly()) {
                    if (prompt.getAnswerValue() != null) {
                        Date d = (Date) prompt.getAnswerObject();
                        view.updateDate(d.getYear() + YEARSHIFT, d.getMonth(), d.getDate());
                    } else {
                        view.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                                .get(Calendar.DAY_OF_MONTH));
                    }
                } else {
                    // handle leap years and number of days in month
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

        if (prompt.getAnswerValue() != null) {
            Date d = (Date) prompt.getAnswerObject();
            mDatePicker.init(d.getYear() + YEARSHIFT, d.getMonth(), d.getDate(), mDateListener);
        } else {
            // create date widget with now
            clearAnswer();
        }

        mDatePicker.setFocusable(!prompt.isReadonly());
        mDatePicker.setEnabled(!prompt.isReadonly());
        
        this.setGravity(Gravity.LEFT);
        this.addView(mDatePicker);
    }

}
