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

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.odk.collect.android.application.Collect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class DateWidget extends QuestionWidget {

    private DatePicker mDatePicker;
    private DatePicker.OnDateChangedListener mDateListener;
    private boolean hideDay = false;
    private boolean hideMonth = false;
    private boolean showCalendar = false;
	private HorizontalScrollView scrollView = null;


    public DateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        mDatePicker = new DatePicker(getContext());
        mDatePicker.setId(QuestionWidget.newUniqueId());
        mDatePicker.setFocusable(!prompt.isReadOnly());
        mDatePicker.setEnabled(!prompt.isReadOnly());

        hideDayFieldIfNotInFormat(prompt);

        mDateListener = new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int month, int day) {
                if (mPrompt.isReadOnly()) {
                    setAnswer();
                } else {
                    // TODO support dates <1900 >2100
                    // handle leap years and number of days in month
                    // http://code.google.com/p/android/issues/detail?id=2081
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, 1);
                    int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                    // in older versions of android (1.6ish) the datepicker lets you pick bad dates
                    // in newer versions, calling updateDate() calls onDatechangedListener(), causing an
                    // endless loop.
                    if (day > max) {
                        if (! (mDatePicker.getDayOfMonth()==day && mDatePicker.getMonth()==month && mDatePicker.getYear()==year) ) {
                        	Collect.getInstance().getActivityLogger().logInstanceAction(DateWidget.this, "onDateChanged",
                        			String.format("%1$04d-%2$02d-%3$02d",year, month, max), mPrompt.getIndex());
                            mDatePicker.updateDate(year, month, max);
                        }
                    } else {
                        if (! (mDatePicker.getDayOfMonth()==day && mDatePicker.getMonth()==month && mDatePicker.getYear()==year) ) {
                        	Collect.getInstance().getActivityLogger().logInstanceAction(DateWidget.this, "onDateChanged",
                        			String.format("%1$04d-%2$02d-%3$02d",year, month, day), mPrompt.getIndex());
                            mDatePicker.updateDate(year, month, day);
                        }
                    }
                }
            }
        };

        setGravity(Gravity.LEFT);
        if ( showCalendar ) {
        	scrollView = new HorizontalScrollView(context);
        	LinearLayout ll = new LinearLayout(context);
        	ll.addView(mDatePicker);
        	ll.setPadding(10, 10, 10, 10);
        	scrollView.addView(ll);
        	addView(scrollView);
        } else {
        	addView(mDatePicker);
        }

        // If there's an answer, use it.
        setAnswer();
    }

    /**
     * Shared between DateWidget and DateTimeWidget.
     * There are extra appearance settings that do not apply for dateTime...
     * TODO: move this into utilities or base class?
     *
     * @param prompt
     */
	@SuppressLint("NewApi")
	private void hideDayFieldIfNotInFormat(FormEntryPrompt prompt) {
        String appearance = prompt.getQuestion().getAppearanceAttr();
        if ( appearance == null ) {
        	if ( Build.VERSION.SDK_INT >= 11 ) {
        		showCalendar = true;
	        	this.mDatePicker.setCalendarViewShown(true);
	        	if ( Build.VERSION.SDK_INT >= 12 ) {
	        		CalendarView cv = this.mDatePicker.getCalendarView();
		        	cv.setShowWeekNumber(false);
	        	}
	        	this.mDatePicker.setSpinnersShown(true);
	        	hideDay = true;
	        	hideMonth = false;
        	} else {
        		return;
        	}
        } else if ( "month-year".equals(appearance) ) {
        	hideDay = true;
        	if ( Build.VERSION.SDK_INT >= 11 ) {
	        	this.mDatePicker.setCalendarViewShown(false);
	        	this.mDatePicker.setSpinnersShown(true);
        	}
        } else if ( "year".equals(appearance) ) {
        	hideMonth = true;
        	if ( Build.VERSION.SDK_INT >= 11 ) {
	        	this.mDatePicker.setCalendarViewShown(false);
	        	this.mDatePicker.setSpinnersShown(true);
        	}
        } else if ("no-calendar".equals(appearance) ) {
        	if ( Build.VERSION.SDK_INT >= 11 ) {
	        	this.mDatePicker.setCalendarViewShown(false);
	        	this.mDatePicker.setSpinnersShown(true);
        	}
        } else {
        	if ( Build.VERSION.SDK_INT >= 11 ) {
        		showCalendar = true;
	        	this.mDatePicker.setCalendarViewShown(true);
	        	if ( Build.VERSION.SDK_INT >= 12 ) {
	        		CalendarView cv = this.mDatePicker.getCalendarView();
	        		cv.setShowWeekNumber(false);
	        	}
	        	this.mDatePicker.setSpinnersShown(true);
	        	hideDay = true;
	        	hideMonth = false;
        	}
        }

        if ( hideMonth || hideDay ) {
		    for (Field datePickerDialogField : this.mDatePicker.getClass().getDeclaredFields()) {
		        if ("mDayPicker".equals(datePickerDialogField.getName()) ||
		                "mDaySpinner".equals(datePickerDialogField.getName())) {
		            datePickerDialogField.setAccessible(true);
		            Object dayPicker = new Object();
		            try {
		                dayPicker = datePickerDialogField.get(this.mDatePicker);
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		            ((View) dayPicker).setVisibility(View.GONE);
		        }
		        if ( hideMonth ) {
			        if ("mMonthPicker".equals(datePickerDialogField.getName()) ||
			                "mMonthSpinner".equals(datePickerDialogField.getName())) {
			            datePickerDialogField.setAccessible(true);
			            Object monthPicker = new Object();
			            try {
			            	monthPicker = datePickerDialogField.get(this.mDatePicker);
			            } catch (Exception e) {
			                e.printStackTrace();
			            }
			            ((View) monthPicker).setVisibility(View.GONE);
			        }
		        }
		    }
        }
    }

    private void setAnswer() {

        if (mPrompt.getAnswerValue() != null) {
            DateTime ldt =
                new DateTime(((Date) ((DateData) mPrompt.getAnswerValue()).getValue()).getTime());
            mDatePicker.init(ldt.getYear(), ldt.getMonthOfYear() - 1, ldt.getDayOfMonth(),
                mDateListener);
        } else {
            // create date widget with current time as of right now
            clearAnswer();
        }
    }


    /**
     * Resets date to today.
     */
    @Override
    public void clearAnswer() {
        DateTime ldt = new DateTime();
        mDatePicker.init(ldt.getYear(), ldt.getMonthOfYear() - 1, ldt.getDayOfMonth(),
            mDateListener);
    }


    @Override
    public IAnswerData getAnswer() {
    	if ( showCalendar ) {
    		scrollView.clearChildFocus(mDatePicker);
    	}
    	clearFocus();
        DateTime ldt =
            new DateTime(mDatePicker.getYear(), (!showCalendar && hideMonth) ? 1 : mDatePicker.getMonth() + 1,
                    (!showCalendar && (hideMonth || hideDay)) ? 1 : mDatePicker.getDayOfMonth(), 0, 0);
        // DateTime utc = ldt.withZone(DateTimeZone.forID("UTC"));
        return new DateData(ldt.toDate());
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
        mDatePicker.setOnLongClickListener(l);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mDatePicker.cancelLongPress();
    }

}
