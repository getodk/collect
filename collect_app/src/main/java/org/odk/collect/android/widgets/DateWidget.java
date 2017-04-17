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

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.DateWidgetUtils;

import java.util.Date;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class DateWidget extends QuestionWidget {
    private DatePickerDialog mDatePickerDialog;

    private Button mDateButton;
    private TextView mDateTextView;

    private boolean mHideDay;
    private boolean mHideMonth;
    private boolean mShowCalendar;

    public DateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        setGravity(Gravity.START);

        createDateButton();
        createDateTextView();
        createDatePickerDialog();
        hideDayFieldIfNotInFormat();
        addViews();

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            DateWidgetUtils.fixCalendarViewIfJellyBean(mDatePickerDialog.getDatePicker().getCalendarView());
        }
    }

    private void hideDayFieldIfNotInFormat() {
        String appearance = mPrompt.getQuestion().getAppearanceAttr();
        if ("month-year".equals(appearance)) {
            mHideDay = true;
        } else if ("year".equals(appearance)) {
            mHideDay = true;
            mHideMonth = true;
        } else if (!"no-calendar".equals(appearance)) {
            mHideDay = true;
            mShowCalendar = true;
            mDatePickerDialog.getDatePicker().setCalendarViewShown(true);
            CalendarView cv = mDatePickerDialog.getDatePicker().getCalendarView();
            cv.setShowWeekNumber(false);
            mDatePickerDialog.getDatePicker().setSpinnersShown(true);
        }

        if (mHideDay) {
            mDatePickerDialog.getDatePicker().findViewById(
                    Resources.getSystem().getIdentifier("day", "id", "android"))
                    .setVisibility(View.GONE);
        }
        if (mHideMonth) {
            mDatePickerDialog.getDatePicker().findViewById(
                    Resources.getSystem().getIdentifier("month", "id", "android"))
                    .setVisibility(View.GONE);
        }
    }

    /**
     * Resets date to today.
     */
    @Override
    public void clearAnswer() {
        DateTime dt = new DateTime();
        mDatePickerDialog.updateDate(dt.getYear(), dt.getMonthOfYear() - 1, dt.getDayOfMonth());
        setDate();
    }

    @Override
    public IAnswerData getAnswer() {
        clearFocus();

        LocalDateTime ldt = new LocalDateTime()
                .withYear(mDatePickerDialog.getDatePicker().getYear())
                .withMonthOfYear((!mShowCalendar && mHideMonth) ? 1 : mDatePickerDialog.getDatePicker().getMonth() + 1)
                .withDayOfMonth((!mShowCalendar && (mHideMonth || mHideDay)) ? 1 : mDatePickerDialog.getDatePicker().getDayOfMonth())
                .withHourOfDay(0)
                .withMinuteOfHour(0);

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
        mDateButton.setOnLongClickListener(l);
        mDateTextView.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mDateButton.cancelLongPress();
        mDateTextView.cancelLongPress();
    }

    private void createDateButton() {
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        mDateButton = new Button(getContext());
        mDateButton.setId(QuestionWidget.newUniqueId());
        mDateButton.setText(R.string.select_date);
        mDateButton.setPadding(20, 20, 20, 20);
        mDateButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mDateButton.setLayoutParams(params);
        mDateButton.setEnabled(!mPrompt.isReadOnly());

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialog.show();
            }
        });
    }

    private void createDateTextView() {
        mDateTextView = new TextView(getContext());
        mDateTextView.setId(QuestionWidget.newUniqueId());
        mDateTextView.setPadding(20, 20, 20, 20);
        mDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
    }

    private void addViews() {
        if (mShowCalendar) {
            HorizontalScrollView scrollView = new HorizontalScrollView(getContext());
            LinearLayout ll = new LinearLayout(getContext());
            ll.addView(mDatePickerDialog.getDatePicker());
            ll.setPadding(10, 10, 10, 10);
            scrollView.addView(ll);
            addAnswerView(scrollView);
        } else {
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(mDateButton);
            linearLayout.addView(mDateTextView);
            addAnswerView(linearLayout);
        }
    }

    private void setDate() {
        mDateTextView.setText(getAnswer().getDisplayText());
    }

    private void createDatePickerDialog() {
        mDatePickerDialog = new CustomDatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mDatePickerDialog.updateDate(year, monthOfYear, dayOfMonth);
                        setDate();
                    }
                }, 0, 0, 0);

        // If there's an answer, use it.
        if (mPrompt.getAnswerValue() != null) {
            // create a new date from date object using default time zone
            DateTime ldt = new DateTime(((Date) mPrompt.getAnswerValue().getValue()).getTime());
            mDatePickerDialog.updateDate(ldt.getYear(), ldt.getMonthOfYear() - 1, ldt.getDayOfMonth());
            setDate();
        } else {
            // create date widget with current time as of right now
            clearAnswer();
        }
    }

    public boolean isDayHidden() {
        return mHideDay;
    }

    public boolean isMonthHidden() {
        return mHideMonth;
    }

    public boolean isCalendarShown() {
        return mShowCalendar;
    }

    public int getYear() {
        return mDatePickerDialog.getDatePicker().getYear();
    }

    public int getMonth() {
        return mDatePickerDialog.getDatePicker().getMonth() + 1;
    }

    public int getDay() {
        return mDatePickerDialog.getDatePicker().getDayOfMonth();
    }

    private class CustomDatePickerDialog extends DatePickerDialog {
        private String mDialogTitle = getContext().getString(R.string.select_date);

        public CustomDatePickerDialog(Context context, OnDateSetListener listener, int year, int month, int dayOfMonth) {
            super(context, listener, year, month, dayOfMonth);
            setTitle(mDialogTitle);
        }

        public void setTitle(CharSequence title) {
            super.setTitle(mDialogTitle);
        }
    }
}
