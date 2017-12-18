/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.odk.collect.android.fragments.dialogs;

import org.javarosa.core.model.FormIndex;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.IslamicChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.util.Arrays;

public class IslamicDatePickerDialog extends CustomDatePickerDialog {

    private static final int MIN_SUPPORTED_YEAR = 1318; //1900 in Gregorian calendar
    private static final int MAX_SUPPORTED_YEAR = 1524; //2100 in Gregorian calendar

    private String[] monthsArray;

    public static IslamicDatePickerDialog newInstance(FormIndex formIndex, LocalDateTime date, DatePickerDetails datePickerDetails) {
        IslamicDatePickerDialog dialog = new IslamicDatePickerDialog();
        dialog.setArguments(getArgs(formIndex, date, datePickerDetails));

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        monthsArray = getResources().getStringArray(R.array.islamic_months);
        setUpValues();
    }

    @Override
    protected void updateDays() {
        setUpDayPicker(getCurrentIslamicDate());
    }

    @Override
    protected LocalDateTime getOriginalDate() {
        return getCurrentIslamicDate();
    }

    private void setUpDatePicker() {
        LocalDateTime islamicDate = DateTimeUtils
                .skipDaylightSavingGapIfExists(getDate())
                .toDateTime()
                .withChronology(IslamicChronology.getInstance())
                .toLocalDateTime();
        setUpDayPicker(islamicDate);
        setUpMonthPicker(islamicDate, monthsArray);
        setUpYearPicker(islamicDate, MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR);
    }

    private void setUpValues() {
        setUpDatePicker();
        updateGregorianDateLabel();
    }

    private LocalDateTime getCurrentIslamicDate() {
        int islamicDay = getDay();
        int islamicMonth = Arrays.asList(monthsArray).indexOf(getMonth());
        int islamicYear = getYear();

        LocalDateTime islamicDate = new LocalDateTime(islamicYear, islamicMonth + 1, 1, 0, 0, 0, 0, IslamicChronology.getInstance());
        if (islamicDay > islamicDate.dayOfMonth().getMaximumValue()) {
            islamicDay = islamicDate.dayOfMonth().getMaximumValue();
        }

        return new LocalDateTime(islamicYear, islamicMonth + 1, islamicDay, 0, 0, 0, 0, IslamicChronology.getInstance());
    }
}
