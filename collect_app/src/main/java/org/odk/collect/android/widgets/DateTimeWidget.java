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

import android.content.Context;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class DateTimeWidget extends QuestionWidget {

    private DateWidget mDateWidget;
    private TimeWidget mTimeWidget;

    public DateTimeWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        setGravity(Gravity.START);

        mDateWidget = new DateWidget(context, prompt);
        mTimeWidget = new TimeWidget(context, prompt);

        mDateWidget.mQuestionMediaLayout.getView_Text().setVisibility(GONE);
        mDateWidget.getHelpTextView().setVisibility(GONE);

        mTimeWidget.mQuestionMediaLayout.getView_Text().setVisibility(GONE);
        mTimeWidget.getHelpTextView().setVisibility(GONE);

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(mDateWidget);
        if (mDateWidget.isCalendarShown() || !mDateWidget.isDayHidden()) {
            linearLayout.addView(mTimeWidget);
        }
        addAnswerView(linearLayout);
        if (mDateWidget.isCalendarShown() && mTimeWidget.getAnswer() == null) {
            mTimeWidget.setTimeToCurrent();
            mTimeWidget.setTimeLabel();
        }
    }

    @Override
    public IAnswerData getAnswer() {
        clearFocus();

        if (mDateWidget.isNullAnswer() && mTimeWidget.isNullAnswer()) {
            return null;
        } else {
            if (mTimeWidget.isNullAnswer()) {
                mTimeWidget.setTimeToCurrent();
                mTimeWidget.setTimeLabel();
            } else if (mDateWidget.isNullAnswer()) {
                mDateWidget.setDateToCurrent();
                mDateWidget.setDateLabel();
            }
            
            boolean hideDay = mDateWidget.isDayHidden();
            boolean hideMonth = mDateWidget.isMonthHidden();
            boolean showCalendar = mDateWidget.isCalendarShown();

            int year = mDateWidget.getYear();
            int month = mDateWidget.getMonth();
            int day = mDateWidget.getDay();
            int hour = mTimeWidget.getHour();
            int minute = mTimeWidget.getMinute();

            LocalDateTime ldt = new LocalDateTime()
                    .withYear(year)
                    .withMonthOfYear((!showCalendar && hideMonth) ? 1 : month)
                    .withDayOfMonth((!showCalendar && (hideMonth || hideDay)) ? 1 : day)
                    .withHourOfDay((!showCalendar && (hideMonth || hideDay)) ? 0 : hour)
                    .withMinuteOfHour((!showCalendar && (hideMonth || hideDay)) ? 0 : minute)
                    .withSecondOfMinute(0);

            ldt = skipDaylightSavingGapIfExists(ldt);
            return new DateTimeData(ldt.toDate());
        }
    }

    @Override
    public void clearAnswer() {
        if (!mDateWidget.isCalendarShown()) {
            mDateWidget.clearAnswer();
            mTimeWidget.clearAnswer();
        }
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
        mDateWidget.setOnLongClickListener(l);
        mTimeWidget.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mDateWidget.cancelLongPress();
        mTimeWidget.cancelLongPress();
    }

    // Skip over a "daylight savings gap". This is needed on the day and time of a daylight savings
    // transition because that date/time doesn't exist.
    // Today clocks are almost always set one hour back or ahead.
    // Throughout history there have been several variations, like half adjustments (30 minutes) or
    // double adjustment (two hours). Adjustments of 20 and 40 minutes have also been used.
    // https://www.timeanddate.com/time/dst/
    private LocalDateTime skipDaylightSavingGapIfExists(LocalDateTime ldt) {
        while (DateTimeZone.getDefault().isLocalDateTimeGap(ldt)) {
            ldt = ldt.plusMinutes(1);
        }
        return ldt;
    }
}
