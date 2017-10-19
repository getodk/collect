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
import org.joda.time.chrono.EthiopicChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.util.Arrays;

/**
 * @author Grzegorz Orczykowski (gorczykowski@soldevelo.com)
 * @author Aurelio Di Pasquale (aurelio.dipasquale@unibas.ch)
 */
public class EthiopianDatePickerDialog extends CustomDatePickerDialog {
    private static final int MIN_SUPPORTED_YEAR = 1893; //1900 in Gregorian calendar
    private static final int MAX_SUPPORTED_YEAR = 2093; //2100 in Gregorian calendar

    private String[] monthsArray;

    public static EthiopianDatePickerDialog newInstance(FormIndex formIndex, LocalDateTime date, DatePickerDetails datePickerDetails) {
        EthiopianDatePickerDialog dialog = new EthiopianDatePickerDialog();
        dialog.setArguments(getArgs(formIndex, date, datePickerDetails));

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        monthsArray = getResources().getStringArray(R.array.ethiopian_months);
        setUpValues();
    }

    @Override
    protected void updateDays() {
        setUpDayPicker(getCurrentEthiopianDate());
    }

    @Override
    protected LocalDateTime getOriginalDate() {
        return getCurrentEthiopianDate();
    }

    private void setUpDatePicker() {
        LocalDateTime ethiopianDate = DateTimeUtils
                .skipDaylightSavingGapIfExists(getDate())
                .toDateTime()
                .withChronology(EthiopicChronology.getInstance())
                .toLocalDateTime();
        setUpDayPicker(ethiopianDate);
        setUpMonthPicker(ethiopianDate, monthsArray);
        setUpYearPicker(ethiopianDate, MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR);
    }

    private void setUpValues() {
        setUpDatePicker();
        updateGregorianDateLabel();
    }

    private LocalDateTime getCurrentEthiopianDate() {
        int ethiopianDay = getDay();
        int ethiopianMonth = Arrays.asList(monthsArray).indexOf(getMonth());
        int ethiopianYear = getYear();

        LocalDateTime ethiopianDate = new LocalDateTime(ethiopianYear, ethiopianMonth + 1, 1, 0, 0, 0, 0, EthiopicChronology.getInstance());
        if (ethiopianDay > ethiopianDate.dayOfMonth().getMaximumValue()) {
            ethiopianDay = ethiopianDate.dayOfMonth().getMaximumValue();
        }

        return new LocalDateTime(ethiopianYear, ethiopianMonth + 1, ethiopianDay, 0, 0, 0, 0, EthiopicChronology.getInstance());
    }
}