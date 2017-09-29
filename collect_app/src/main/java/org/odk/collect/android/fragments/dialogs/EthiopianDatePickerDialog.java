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
        setUpDayPicker(getCurrentEthiopianDateDisplay());
    }

    @Override
    protected DateTime getOriginalDate() {
        return getCurrentEthiopianDateDisplay();
    }

    private void setUpDatePicker(DateTime gregorianDate) {
        DateTime ethiopianDate = gregorianDate.withChronology(EthiopicChronology.getInstance());
        setUpDayPicker(ethiopianDate);
        setUpMonthPicker(ethiopianDate);
        setUpYearPicker(ethiopianDate);
    }

    private void setUpDayPicker(DateTime ethiopianDate) {
        int maxDay = ethiopianDate.dayOfMonth().getMaximumValue();
        dayPicker.setDisplayedValues(null);
        dayPicker.setMaxValue(maxDay - 1);
        dayPicker.setDisplayedValues(getDayPickerValues(maxDay));
        dayPicker.setValue(ethiopianDate.getDayOfMonth() - 1);
    }

    private void setUpMonthPicker(DateTime ethiopianDate) {
        monthPicker.setDisplayedValues(null);
        monthPicker.setMaxValue(monthsArray.length - 1);
        monthPicker.setDisplayedValues(monthsArray);
        monthPicker.setValue(ethiopianDate.getMonthOfYear() - 1);
    }

    private void setUpYearPicker(DateTime ethiopianDate) {
        int maxYear = 2200;
        String[] years = getYearPickerValues(maxYear);
        yearPicker.setDisplayedValues(null);
        yearPicker.setMaxValue(maxYear - 1);
        yearPicker.setDisplayedValues(getYearPickerValues(maxYear));
        yearPicker.setValue(Arrays.asList(years).indexOf(String.valueOf(ethiopianDate.getYear())));
    }

    private void setUpValues() {
        setUpDatePicker(dateTime);
        updateGregorianDateLabel();
    }

    private DateTime getCurrentEthiopianDateDisplay() {
        int ethiopianDay = Integer.parseInt(dayPicker.getDisplayedValues()[dayPicker.getValue()]);
        int ethiopianMonth = Arrays.asList(monthsArray).indexOf(monthPicker.getDisplayedValues()[monthPicker.getValue()]);
        int ethiopianYear = Integer.parseInt(yearPicker.getDisplayedValues()[yearPicker.getValue()]);

        DateTime ethiopianDate = new DateTime(ethiopianYear, ethiopianMonth + 1, 1, 0, 0, 0, 0, EthiopicChronology.getInstance());
        if (ethiopianDay > ethiopianDate.dayOfMonth().getMaximumValue()) {
            ethiopianDay = ethiopianDate.dayOfMonth().getMaximumValue();
        }

        return new DateTime(ethiopianYear, ethiopianMonth + 1, ethiopianDay, 0, 0, 0, 0, EthiopicChronology.getInstance());
    }

    private String[] getDayPickerValues(int maxDay) {
        String[] days = new String[maxDay];
        for (int i = 1; i <= maxDay; i++) {
            days[i - 1] = String.valueOf(i);
        }

        return days;
    }

    private String[] getYearPickerValues(int maxYear) {
        String[] years = new String[maxYear];
        for (int i = 1; i <= maxYear; i++) {
            years[i - 1] = String.valueOf(i);
        }

        return years;
    }
}