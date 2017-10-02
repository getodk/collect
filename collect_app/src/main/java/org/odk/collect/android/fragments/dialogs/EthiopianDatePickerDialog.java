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
import org.joda.time.DateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.widgets.AbstractDateWidget;

import java.util.Arrays;

/**
 * @author Grzegorz Orczykowski (gorczykowski@soldevelo.com)
 * @author Aurelio Di Pasquale (aurelio.dipasquale@unibas.ch)
 */
public class EthiopianDatePickerDialog extends CustomDatePickerDialog {
    private static final int MIN_SUPPORTED_YEAR = 1893; //1900 in Gregorian calendar
    private static final int MAX_SUPPORTED_YEAR = 2093; //2100 in Gregorian calendar

    private String[] monthsArray;

    public static EthiopianDatePickerDialog newInstance(FormIndex formIndex, DateTime dateTime, AbstractDateWidget.CalendarMode calendarMode) {
        EthiopianDatePickerDialog dialog = new EthiopianDatePickerDialog();
        dialog.setArguments(getArgs(formIndex, dateTime, calendarMode));

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
    protected DateTime getOriginalDate() {
        return getCurrentEthiopianDate();
    }

    private void setUpDatePicker(DateTime gregorianDate) {
        DateTime ethiopianDate = gregorianDate.withChronology(EthiopicChronology.getInstance());
        setUpDayPicker(ethiopianDate);
        setUpMonthPicker(ethiopianDate);
        setUpYearPicker(ethiopianDate);
    }

    private void setUpDayPicker(DateTime ethiopianDate) {
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(ethiopianDate.dayOfMonth().getMaximumValue());
        dayPicker.setValue(ethiopianDate.getDayOfMonth());
    }

    private void setUpMonthPicker(DateTime ethiopianDate) {
        monthPicker.setMaxValue(monthsArray.length - 1);
        monthPicker.setDisplayedValues(monthsArray);
        monthPicker.setValue(ethiopianDate.getMonthOfYear() - 1);
    }

    private void setUpYearPicker(DateTime ethiopianDate) {
        yearPicker.setMinValue(MIN_SUPPORTED_YEAR);
        yearPicker.setMaxValue(MAX_SUPPORTED_YEAR);
        yearPicker.setValue(ethiopianDate.getYear());
    }

    private void setUpValues() {
        setUpDatePicker(dateTime);
        updateGregorianDateLabel();
    }

    private DateTime getCurrentEthiopianDate() {
        int ethiopianDay = dayPicker.getValue();
        int ethiopianMonth = Arrays.asList(monthsArray).indexOf(monthPicker.getDisplayedValues()[monthPicker.getValue()]);
        int ethiopianYear = yearPicker.getValue();

        DateTime ethiopianDate = new DateTime(ethiopianYear, ethiopianMonth + 1, 1, 0, 0, 0, 0, EthiopicChronology.getInstance());
        if (ethiopianDay > ethiopianDate.dayOfMonth().getMaximumValue()) {
            ethiopianDay = ethiopianDate.dayOfMonth().getMaximumValue();
        }

        return new DateTime(ethiopianYear, ethiopianMonth + 1, ethiopianDay, 0, 0, 0, 0, EthiopicChronology.getInstance());
    }
}