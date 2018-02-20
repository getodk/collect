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
import org.joda.time.chrono.CopticChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.util.Arrays;

public class CopticDatePickerDialog extends CustomDatePickerDialog {
    private static final int MIN_SUPPORTED_YEAR = 1617; //1900 in Gregorian calendar
    private static final int MAX_SUPPORTED_YEAR = 1817; //2100 in Gregorian calendar

    private String[] monthsArray;

    public static CopticDatePickerDialog newInstance(FormIndex formIndex, LocalDateTime date, DatePickerDetails datePickerDetails) {
        CopticDatePickerDialog dialog = new CopticDatePickerDialog();
        dialog.setArguments(getArgs(formIndex, date, datePickerDetails));

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        monthsArray = getResources().getStringArray(R.array.coptic_months);
        setUpValues();
    }

    @Override
    protected void updateDays() {
        setUpDayPicker(getCurrentCopticDate());
    }

    @Override
    protected LocalDateTime getOriginalDate() {
        return getCurrentCopticDate();
    }

    private void setUpDatePicker() {
        LocalDateTime copticDate = DateTimeUtils
                .skipDaylightSavingGapIfExists(getDate())
                .toDateTime()
                .withChronology(CopticChronology.getInstance())
                .toLocalDateTime();
        setUpDayPicker(copticDate);
        setUpMonthPicker(copticDate, monthsArray);
        setUpYearPicker(copticDate, MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR);
    }

    private void setUpValues() {
        setUpDatePicker();
        updateGregorianDateLabel();
    }

    private LocalDateTime getCurrentCopticDate() {
        int copticDay = getDay();
        int copticMonth = Arrays.asList(monthsArray).indexOf(getMonth());
        int copticYear = getYear();

        LocalDateTime copticDate = new LocalDateTime(copticYear, copticMonth + 1, 1, 0, 0, 0, 0, CopticChronology.getInstance());
        if (copticDay > copticDate.dayOfMonth().getMaximumValue()) {
            copticDay = copticDate.dayOfMonth().getMaximumValue();
        }

        return new LocalDateTime(copticYear, copticMonth + 1, copticDay, 0, 0, 0, 0, CopticChronology.getInstance());
    }
}
