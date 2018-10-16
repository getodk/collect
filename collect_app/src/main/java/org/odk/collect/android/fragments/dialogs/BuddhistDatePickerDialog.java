/*
 * Copyright 2018 Nafundi
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
import org.joda.time.chrono.BuddhistChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.util.Arrays;

public class BuddhistDatePickerDialog extends CustomDatePickerDialog {
    private static final int MIN_SUPPORTED_YEAR = 2443; //1900 in Gregorian calendar
    private static final int MAX_SUPPORTED_YEAR = 2643; //2100 in Gregorian calendar

    private String[] monthsArray;

    public static BuddhistDatePickerDialog newInstance(FormIndex formIndex, LocalDateTime date, DatePickerDetails datePickerDetails) {
        BuddhistDatePickerDialog dialog = new BuddhistDatePickerDialog();
        dialog.setArguments(getArgs(formIndex, date, datePickerDetails));

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        monthsArray = getResources().getStringArray(R.array.buddhist_months);
        setUpValues();
    }

    @Override
    protected void updateDays() {
        LocalDateTime localDateTime = getCurrentBuddhistDate();
        setUpDayPicker(localDateTime.getDayOfMonth(), localDateTime.dayOfMonth().getMaximumValue());
    }

    @Override
    protected LocalDateTime getOriginalDate() {
        return getCurrentBuddhistDate();
    }

    private void setUpDatePicker() {
        LocalDateTime buddhistDate = DateTimeUtils
                .skipDaylightSavingGapIfExists(getDate())
                .toDateTime()
                .withChronology(BuddhistChronology.getInstance())
                .toLocalDateTime();
        setUpDayPicker(buddhistDate.getDayOfMonth(), buddhistDate.dayOfMonth().getMaximumValue());
        setUpMonthPicker(buddhistDate.getMonthOfYear(), monthsArray);
        setUpYearPicker(buddhistDate.getYear(), MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR);
    }

    private void setUpValues() {
        setUpDatePicker();
        updateGregorianDateLabel();
    }

    private LocalDateTime getCurrentBuddhistDate() {
        int buddhistDay = getDay();
        int buddhistMonth = Arrays.asList(monthsArray).indexOf(getMonth());
        int buddhistYear = getYear();

        LocalDateTime buddhistDate = new LocalDateTime(buddhistYear, buddhistMonth + 1, 1, 0, 0, 0, 0, BuddhistChronology.getInstance());
        if (buddhistDay > buddhistDate.dayOfMonth().getMaximumValue()) {
            buddhistDay = buddhistDate.dayOfMonth().getMaximumValue();
        }

        return new LocalDateTime(buddhistYear, buddhistMonth + 1, buddhistDay, 0, 0, 0, 0, BuddhistChronology.getInstance());
    }
}