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
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.application.Collect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class DateTimeWidget extends QuestionWidget {

    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private DatePicker.OnDateChangedListener mDateListener;
    private boolean hideDay = false;
    private boolean hideMonth = false;
    private boolean showCalendar = false;
    private HorizontalScrollView scrollView = null;

    public DateTimeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        mDatePicker = new DatePicker(getContext());
        mDatePicker.setId(QuestionWidget.newUniqueId());
        mDatePicker.setFocusable(!prompt.isReadOnly());
        mDatePicker.setEnabled(!prompt.isReadOnly());

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) { // this bug exists only in Android 4.1
            fixCalendarViewIfJellyBean(mDatePicker.getCalendarView());
        }
        mTimePicker = new TimePicker(getContext());
        mTimePicker.setId(QuestionWidget.newUniqueId());
        mTimePicker.setFocusable(!prompt.isReadOnly());
        mTimePicker.setEnabled(!prompt.isReadOnly());
        mTimePicker.setPadding(0, 20, 0, 0);

        String clockType =
                android.provider.Settings.System.getString(context.getContentResolver(),
                        android.provider.Settings.System.TIME_12_24);
        if (clockType == null || clockType.equalsIgnoreCase("24")) {
            mTimePicker.setIs24HourView(true);
        }

        hideDayFieldIfNotInFormat(prompt);

        mDateListener = new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int month, int day) {
                if (mPrompt.isReadOnly()) {
                    setAnswer();
                } else {
                    // handle leap years and number of days in month
                    // TODO
                    // http://code.google.com/p/android/issues/detail?id=2081
                    // in older versions of android (1.6ish) the datepicker lets you pick bad dates
                    // in newer versions, calling updateDate() calls onDatechangedListener(),
                    // causing an
                    // endless loop.
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, 1);
                    int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (day > max) {
                        if (!(mDatePicker.getDayOfMonth() == day && mDatePicker.getMonth() == month
                                && mDatePicker.getYear() == year)) {
                            Collect.getInstance().getActivityLogger().logInstanceAction(
                                    DateTimeWidget.this, "onDateChanged",
                                    String.format("%1$04d-%2$02d-%3$02d", year, month, max),
                                    mPrompt.getIndex());
                            mDatePicker.updateDate(year, month, max);
                        }
                    } else {
                        if (!(mDatePicker.getDayOfMonth() == day && mDatePicker.getMonth() == month
                                && mDatePicker.getYear() == year)) {
                            Collect.getInstance().getActivityLogger().logInstanceAction(
                                    DateTimeWidget.this, "onDateChanged",
                                    String.format("%1$04d-%2$02d-%3$02d", year, month, day),
                                    mPrompt.getIndex());
                            mDatePicker.updateDate(year, month, day);
                        }
                    }
                }
            }
        };

        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                Collect.getInstance().getActivityLogger().logInstanceAction(DateTimeWidget.this,
                        "onTimeChanged",
                        String.format("%1$02d:%2$02d", hourOfDay, minute), mPrompt.getIndex());
            }
        });

        setGravity(Gravity.START);
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        if (showCalendar) {
            scrollView = new HorizontalScrollView(context);
            LinearLayout ll = new LinearLayout(context);
            ll.addView(mDatePicker);
            ll.setPadding(10, 10, 10, 10);
            scrollView.addView(ll);
            answerLayout.addView(scrollView);
        } else {
            answerLayout.addView(mDatePicker);
        }
        answerLayout.addView(mTimePicker);
        addAnswerView(answerLayout);

        // If there's an answer, use it.
        setAnswer();
    }

    private void fixCalendarViewIfJellyBean(CalendarView calendarView) {
        try {
            Object object = calendarView;
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("mDelegate")) { // the CalendarViewLegacyDelegate instance is stored in this variable
                    field.setAccessible(true);
                    object = field.get(object);
                    break;
                }
            }

            Field field = object.getClass().getDeclaredField("mDateTextSize"); // text size integer value
            field.setAccessible(true);
            final int mDateTextSize = (Integer) field.get(object);

            field = object.getClass().getDeclaredField("mListView"); // main ListView
            field.setAccessible(true);
            Object innerObject = field.get(object);

            Method method = innerObject.getClass().getMethod(
                    "setOnHierarchyChangeListener", ViewGroup.OnHierarchyChangeListener.class); // we need to set the OnHierarchyChangeListener
            method.setAccessible(true);
            method.invoke(innerObject, (Object) new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) { // apply text size every time when a new child view is added
                    try {
                        Object object = child;
                        Field[] fields = object.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            if (field.getName().equals("mMonthNumDrawPaint")) { // the paint is stored inside the view
                                field.setAccessible(true);
                                object = field.get(object);
                                Method method = object.getClass().
                                        getDeclaredMethod("setTextSize", float.class); // finally set text size
                                method.setAccessible(true);
                                method.invoke(object, (Object) mDateTextSize);

                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e("DateTimeWidget", e.getMessage(), e);
                    }
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {
                }
            });
        } catch (Exception e) {
            Log.e("DateTimeWidget", e.getMessage(), e);
        }
    }

    /**
     * Shared between DateWidget and DateTimeWidget.
     * There are extra appearance settings that do not apply for dateTime...
     * TODO: move this into utilities or base class?
     */
    @SuppressLint("NewApi")
    private void hideDayFieldIfNotInFormat(FormEntryPrompt prompt) {
        String appearance = prompt.getQuestion().getAppearanceAttr();
        if (appearance == null) {
            showCalendar = true;
            this.mDatePicker.setCalendarViewShown(true);
            CalendarView cv = this.mDatePicker.getCalendarView();
            cv.setShowWeekNumber(false);
            this.mDatePicker.setSpinnersShown(true);
            hideDay = true;
            hideMonth = false;
        } else if ("month-year".equals(appearance)) {
            hideDay = true;
            this.mDatePicker.setCalendarViewShown(false);
            this.mDatePicker.setSpinnersShown(true);
            mTimePicker.setVisibility(GONE);
        } else if ("year".equals(appearance)) {
            hideMonth = true;
            this.mDatePicker.setCalendarViewShown(false);
            this.mDatePicker.setSpinnersShown(true);
            mTimePicker.setVisibility(GONE);
        } else if ("no-calendar".equals(appearance)) {
            this.mDatePicker.setCalendarViewShown(false);
            this.mDatePicker.setSpinnersShown(true);
        } else {
            showCalendar = true;
            this.mDatePicker.setCalendarViewShown(true);
            CalendarView cv = this.mDatePicker.getCalendarView();
            cv.setShowWeekNumber(false);
            this.mDatePicker.setSpinnersShown(true);
            hideDay = true;
            hideMonth = false;
        }

        if (hideMonth || hideDay) {
            mDatePicker.findViewById(
                    Resources.getSystem().getIdentifier("day", "id", "android"))
                    .setVisibility(View.GONE);
            if (hideMonth) {
                mDatePicker
                        .findViewById(
                                Resources.getSystem().getIdentifier("month", "id", "android"))
                        .setVisibility(View.GONE);
            }
        }
    }

    private void setAnswer() {

        if (mPrompt.getAnswerValue() != null) {

            DateTime ldt =
                    new DateTime(
                            ((Date) mPrompt.getAnswerValue().getValue()).getTime
                                    ());
            mDatePicker.init(ldt.getYear(), ldt.getMonthOfYear() - 1, ldt.getDayOfMonth(),
                    mDateListener);
            mTimePicker.setCurrentHour(ldt.getHourOfDay());
            mTimePicker.setCurrentMinute(ldt.getMinuteOfHour());

        } else {
            // create time widget with current time as of right now
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
        mTimePicker.setCurrentHour(ldt.getHourOfDay());
        mTimePicker.setCurrentMinute(ldt.getMinuteOfHour());
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
                .withHourOfDay((!showCalendar && (hideMonth || hideDay)) ? 0 : mTimePicker.getCurrentHour())
                .withMinuteOfHour((!showCalendar && (hideMonth || hideDay)) ? 0 : mTimePicker.getCurrentMinute())
                .withSecondOfMinute(0);

        ldt = skipDaylightSavingGapIfExists(ldt);
        return new DateTimeData(ldt.toDate());
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
        mTimePicker.setOnLongClickListener(l);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mDatePicker.cancelLongPress();
        mTimePicker.cancelLongPress();
    }

}
