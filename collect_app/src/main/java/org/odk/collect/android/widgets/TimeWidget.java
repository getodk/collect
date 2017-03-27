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

import android.app.TimePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.odk.collect.android.R;

import java.util.Date;

/**
 * Displays a TimePicker widget.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class TimeWidget extends QuestionWidget {
    private TimePickerDialog mTimePickerDialog;

    private Button mTimeButton;
    private TextView mTimeTextView;

    private int mHourOfDay;
    private int mMinuteOfHour;

    public TimeWidget(Context context, final FormEntryPrompt prompt) {
        super(context, prompt);

        setGravity(Gravity.LEFT);

        createTimeButton();
        createTimeTextView();
        createTimePickerDialog();
        addViews();
    }

    /**
     * Resets time to today.
     */
    @Override
    public void clearAnswer() {
        DateTime dt = new DateTime();
        setTime(dt.getHourOfDay(), dt.getMinuteOfHour());
        mTimePickerDialog.updateTime(mHourOfDay, mMinuteOfHour);
    }

    @Override
    public IAnswerData getAnswer() {
        clearFocus();
        // use picker time, convert to today's date, store as utc
        DateTime ldt = (new DateTime()).withTime(mHourOfDay, mMinuteOfHour, 0, 0);
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
        mTimeButton.setOnLongClickListener(l);
        mTimeTextView.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mTimeButton.cancelLongPress();
        mTimeTextView.cancelLongPress();
    }

    private void createTimeButton() {
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        mTimeButton = new Button(getContext());
        mTimeButton.setId(QuestionWidget.newUniqueId());
        mTimeButton.setText(R.string.select_time);
        mTimeButton.setPadding(20, 20, 20, 20);
        mTimeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mTimeButton.setLayoutParams(params);
        mTimeButton.setEnabled(!mPrompt.isReadOnly());

        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimePickerDialog.updateTime(mHourOfDay, mMinuteOfHour);
                mTimePickerDialog.show();
            }
        });
    }

    private void createTimeTextView() {
        mTimeTextView = new TextView(getContext());
        mTimeTextView.setId(QuestionWidget.newUniqueId());
        mTimeTextView.setPadding(20, 20, 20, 20);
        mTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
    }

    private void addViews() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(mTimeButton);
        linearLayout.addView(mTimeTextView);
        addAnswerView(linearLayout);
    }

    private void setTime(int hourOfDay, int minuteOfHour) {
        mHourOfDay = hourOfDay;
        mMinuteOfHour = minuteOfHour;

        String hour = mHourOfDay < 10 ? "0" + mHourOfDay : "" + mHourOfDay;
        String minute = mMinuteOfHour < 10 ? "0" + mMinuteOfHour : "" + mMinuteOfHour;

        mTimeTextView.setText(hour + ":" + minute);
    }

    private void createTimePickerDialog() {
        mTimePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                        setTime(hourOfDay, minuteOfHour);
                    }
                }, 0, 0, DateFormat.is24HourFormat(getContext()));

        // If there's an answer, use it.
        if (mPrompt.getAnswerValue() != null) {
            // create a new date time from date object using default time zone
            DateTime dt = new DateTime(((Date) mPrompt.getAnswerValue().getValue()).getTime());
            setTime(dt.getHourOfDay(), dt.getMinuteOfHour());
            mTimePickerDialog.updateTime(mHourOfDay, mMinuteOfHour);
        } else {
            // create time widget with current time as of right now
            clearAnswer();
        }
    }
}
