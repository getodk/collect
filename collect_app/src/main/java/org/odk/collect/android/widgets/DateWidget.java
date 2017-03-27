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

import android.annotation.SuppressLint;
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
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.DateWidgetUtils;

import java.util.Calendar;
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

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            DateWidgetUtils.fixCalendarViewIfJellyBean(mDatePicker.getCalendarView());
        }

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
                    // in newer versions, calling updateDate() calls onDatechangedListener(),
                    // causing an
                    // endless loop.
                    if (day > max) {
                        if (!(mDatePicker.getDayOfMonth() == day && mDatePicker.getMonth() == month
                                && mDatePicker.getYear() == year)) {
                            Collect.getInstance().getActivityLogger().logInstanceAction(
                                    DateWidget.this, "onDateChanged",
                                    String.format("%1$04d-%2$02d-%3$02d", year, month, max),
                                    mPrompt.getIndex());
                            mDatePicker.updateDate(year, month, max);
                        }
                    } else {
                        if (!(mDatePicker.getDayOfMonth() == day && mDatePicker.getMonth() == month
                                && mDatePicker.getYear() == year)) {
                            Collect.getInstance().getActivityLogger().logInstanceAction(
                                    DateWidget.this, "onDateChanged",
                                    String.format("%1$04d-%2$02d-%3$02d", year, month, day),
                                    mPrompt.getIndex());
                            mDatePicker.updateDate(year, month, day);
                        }
                    }
                }
            }
        };

        setGravity(Gravity.LEFT);
        if (showCalendar) {
            scrollView = new HorizontalScrollView(context);
            LinearLayout ll = new LinearLayout(context);
            ll.addView(mDatePicker);
            ll.setPadding(10, 10, 10, 10);
            scrollView.addView(ll);
            addAnswerView(scrollView);
        } else {
            addAnswerView(mDatePicker);
        }

        // If there's an answer, use it.
        setAnswer();


        createDateButton();
        createDateTextView();
        createDatePickerDialog();
        addViews();
        hideDayFieldIfNotInFormat();
    }

    private void hideDayFieldIfNotInFormat() {
        String appearance = mPrompt.getQuestion().getAppearanceAttr();
        if ("month-year".equals(appearance)) {
            hideDay = true;
        } else if ("year".equals(appearance)) {
            hideDay = true;
            hideMonth = true;
        } else if ("no-calendar".equals(appearance)) {
        } else {
            showCalendar = true;
            hideDay = true;
        }

        if (hideMonth || hideDay) {
            mDatePickerDialog.getDatePicker().findViewById(
                    Resources.getSystem().getIdentifier("day", "id", "android"))
                    .setVisibility(View.GONE);
            if (hideMonth) {
                mDatePickerDialog.getDatePicker().findViewById(
                        Resources.getSystem().getIdentifier("month", "id", "android"))
                        .setVisibility(View.GONE);
            }
        }
    }

    private void setAnswer() {

        if (mPrompt.getAnswerValue() != null) {
            DateTime ldt =
                    new DateTime(
                            ((Date) mPrompt.getAnswerValue().getValue()).getTime());
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
        if (showCalendar) {
            scrollView.clearChildFocus(mDatePicker);
        }
        clearFocus();

        LocalDateTime ldt = new LocalDateTime()
                .withYear(mDatePicker.getYear())
                .withMonthOfYear((!showCalendar && hideMonth) ? 1 : mDatePicker.getMonth() + 1)
                .withDayOfMonth((!showCalendar && (hideMonth || hideDay)) ? 1 : mDatePicker.getDayOfMonth())
                .withHourOfDay(0)
                .withMinuteOfHour(0);

        ldt = skipDaylightSavingGapIfExists(ldt);
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
        mDateButton.setText(R.string.select_time);
        mDateButton.setPadding(20, 20, 20, 20);
        mDateButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mDateButton.setLayoutParams(params);
        mDateButton.setEnabled(!mPrompt.isReadOnly());

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(mDateButton);
        linearLayout.addView(mDateTextView);
        addAnswerView(linearLayout);
    }

    private void createDatePickerDialog() {
        mDatePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    }
                }, 0, 0, 0);

        // If there's an answer, use it.
        if (mPrompt.getAnswerValue() != null) {
            // create a new date from date object using default time zone
            DateTime ldt = new DateTime(((Date) mPrompt.getAnswerValue().getValue()).getTime());
            mDatePickerDialog.updateDate(ldt.getYear(), ldt.getMonthOfYear() - 1, ldt.getDayOfMonth());
        } else {
            // create date widget with current time as of right now
            clearAnswer();
        }
    }
}
