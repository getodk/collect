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

import org.joda.time.LocalDateTime;

import java.util.Arrays;

import bikramsambat.BikramSambatDate;
import bikramsambat.BsCalendar;
import bikramsambat.BsException;
import bikramsambat.BsGregorianDate;
import timber.log.Timber;

public class BikramSambatDatePickerDialog extends CustomDatePickerDialog {
    private static final int MIN_SUPPORTED_YEAR = 1970; //1913 in Gregorian calendar
    private static final int MAX_SUPPORTED_YEAR = 2090; //2033 in Gregorian calendar

    private final String[] monthsArray = BsCalendar.MONTH_NAMES.toArray(new String[0]);

    @Override
    public void onResume() {
        super.onResume();
        setUpValues();
    }

    @Override
    protected void updateDays() {
        BikramSambatDate bikramSambatDate = new BikramSambatDate(getYear(), Arrays.asList(monthsArray).indexOf(getMonth()) + 1, getDay());
        int daysInMonth = 0;
        try {
            daysInMonth = BsCalendar.getInstance().daysInMonth(bikramSambatDate.year, bikramSambatDate.month);
        } catch (BsException e) {
            Timber.e(e);
        }
        setUpDayPicker(bikramSambatDate.day, daysInMonth);
    }

    @Override
    protected LocalDateTime getOriginalDate() {
        BsGregorianDate bsGregorianDate = null;
        try {
            bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(getYear(), Arrays.asList(monthsArray).indexOf(getMonth()) + 1, getDay()));
        } catch (BsException e) {
            Timber.e(e);
        }

        return new LocalDateTime()
                .withYear(bsGregorianDate.year)
                .withMonthOfYear(bsGregorianDate.month)
                .withDayOfMonth(bsGregorianDate.day)
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
    }

    private void setUpDatePicker() {
        LocalDateTime localDateTime = getDate();
        try {
            BikramSambatDate bikramSambatDate = BsCalendar.getInstance().toBik(localDateTime.getYear(), localDateTime.getMonthOfYear(), localDateTime.getDayOfMonth());
            setUpDayPicker(bikramSambatDate.day, BsCalendar.getInstance().daysInMonth(bikramSambatDate.year, bikramSambatDate.month));
            setUpMonthPicker(bikramSambatDate.month, monthsArray);
            setUpYearPicker(bikramSambatDate.year, MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR);
        } catch (BsException e) {
            Timber.e(e);
        }
    }

    private void setUpValues() {
        setUpDatePicker();
        updateGregorianDateLabel();
    }
}
