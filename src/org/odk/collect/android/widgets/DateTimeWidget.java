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

import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.widgets.AbstractQuestionWidget.OnDescendantRequestFocusChangeListener.FocusChangeState;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.widget.DatePicker;
import android.widget.TimePicker;

/**
 * Displays a Timepicker widget. TimeWidget
 * 
 * @author Aurelio Di Pasquale aurdipas@gmail.com
 */
public class DateTimeWidget extends AbstractQuestionWidget {

	// Holds the current date. Used for determining if day of month is too big.
	// Not in handler because constructor takes a while...
	private final Calendar currentDate = new GregorianCalendar(); // now...

	private TimePicker mTimePicker;
	private DatePicker mDatePicker;
	private DatePicker.OnDateChangedListener mDateListener;

	public DateTimeWidget(Handler handler, Context context,
			FormEntryPrompt prompt) {
		super(handler, context, prompt);
	}

	@Override
	public IAnswerData getAnswer() {
		// clear focus first so the timewidget gets the value in the text box
		mDatePicker.clearFocus();
		mTimePicker.clearFocus();
		GregorianCalendar c = new GregorianCalendar(mDatePicker.getYear(),
				mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
		Date d = c.getTime();
		d.setHours(mTimePicker.getCurrentHour());
		d.setMinutes(mTimePicker.getCurrentMinute());
		return new DateTimeData(d);
	}

	/**
	 * Build view for time answer. Includes retrieving existing answer.
	 */
	@Override
	protected void buildViewBodyImpl() {

		mDatePicker = new DatePicker(getContext());
		mDatePicker.setFocusable(!prompt.isReadOnly());
		mDatePicker.setEnabled(!prompt.isReadOnly());

		mDateListener = new DatePicker.OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int year, int month,
					int day) {
				// TODO: MES -- is this broken if calculated date?
				// TODO: MES -- Or if calculated readonly() field?
				if (!prompt.isReadOnly()) {
					// handle leap years and number of days in month
					// TODO
					// http://code.google.com/p/android/issues/detail?id=2081
					currentDate.set(year, month, 1);
					int max = currentDate
							.getActualMaximum(Calendar.DAY_OF_MONTH);
					if (day > max) {
						view.updateDate(year, month, max);
					} else {
						view.updateDate(year, month, day);
					}
				}
				// gain focus after change because we might have a
				// constraint violation somewhere else that will
				// restore focus elsewhere
				signalDescendant(FocusChangeState.DIVERGE_VIEW_FROM_MODEL);
			}
		};

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
		addView(mDatePicker);
		addView(mTimePicker);
	}

	protected void updateViewAfterAnswer() {
		IAnswerData answer = prompt.getAnswerValue();
		if (answer == null) {
			final Calendar c = new GregorianCalendar();
			mDatePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
					.get(Calendar.DAY_OF_MONTH), mDateListener);
			mTimePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
			mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
		} else {
			final Calendar c = new GregorianCalendar();
			c.setTime((Date) prompt.getAnswerValue().getValue());
			mDatePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
					.get(Calendar.DAY_OF_MONTH), mDateListener);
			mTimePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
			mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
		}
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		mDatePicker.setEnabled(isEnabled && !prompt.isReadOnly());
		mTimePicker.setEnabled(isEnabled && !prompt.isReadOnly());
	}

}
