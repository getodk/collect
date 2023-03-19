/*
 * Copyright 2019 Nafundi
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
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.utilities.MyanmarDateUtils;

import java.util.List;

import mmcalendar.MyanmarDate;
import mmcalendar.MyanmarDateKernel;

public class MyanmarDatePickerDialog extends CustomDatePickerDialog {
    private static final int MIN_SUPPORTED_YEAR = 1261; //1900 in Gregorian calendar
    private static final int MAX_SUPPORTED_YEAR = 1462; //2100 in Gregorian calendar

    @Override
    public void onResume() {
        super.onResume();
        setUpDatePicker();
        updateGregorianDateLabel();
    }

    @Override
    protected void updateDays() {
        MyanmarDate myanmarDate = getCurrentMyanmarDate();
        setUpDayPicker(MyanmarDateUtils.getFirstMonthDay(myanmarDate), myanmarDate.getMonthDay(), MyanmarDateUtils.getMonthLength(myanmarDate));
    }

    @Override
    protected void yearUpdated() {
        MyanmarDate myanmarDate = getCurrentMyanmarDate();
        setUpMonthPicker(MyanmarDateUtils.getMonthId(myanmarDate) + 1, MyanmarDateUtils.getMyanmarMonthsArray(myanmarDate.getYearInt()));
        super.yearUpdated();
    }

    @Override
    protected LocalDateTime getOriginalDate() {
        return MyanmarDateUtils.myanmarDateToGregorianDate(getCurrentMyanmarDate());
    }

    private void setUpDatePicker() {
        MyanmarDate myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(DateTimeUtils.getDateWithSkippedDaylightSavingGapIfExists(getDate()));

        setUpDayPicker(MyanmarDateUtils.getFirstMonthDay(myanmarDate), myanmarDate.getMonthDay(), MyanmarDateUtils.getMonthLength(myanmarDate));
        setUpMonthPicker(MyanmarDateUtils.getMonthId(myanmarDate) + 1, MyanmarDateUtils.getMyanmarMonthsArray(myanmarDate.getYearInt()));
        setUpYearPicker(myanmarDate.getYearInt(), MIN_SUPPORTED_YEAR, MAX_SUPPORTED_YEAR);
    }

    private MyanmarDate getCurrentMyanmarDate() {
        List<Integer> monthIndexes = MyanmarDateKernel.getMyanmarMonth(getYear(), 1).getIndex();
        int monthIndex = getMonthId() < monthIndexes.size() ? monthIndexes.get(getMonthId()) : monthIndexes.get(monthIndexes.size() - 1);
        int monthLength = MyanmarDateUtils.getMonthLength(getYear(), monthIndex);
        int dayOfMonth = getDay() > monthLength ? monthLength : getDay();

        return MyanmarDateUtils.createMyanmarDate(getYear(), monthIndex, dayOfMonth);
    }
}