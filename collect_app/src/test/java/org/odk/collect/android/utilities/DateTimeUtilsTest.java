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

package org.odk.collect.android.utilities;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.logic.DatePickerDetails;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DateTimeUtilsTest {

    private DatePickerDetails gregorianCalendar;
    private DatePickerDetails gregorianSpinners;
    private DatePickerDetails gregorianMonthYear;
    private DatePickerDetails gregorianYear;
    private DatePickerDetails ethiopian;
    private DatePickerDetails ethiopianMonthYear;
    private DatePickerDetails ethiopianYear;


    @Before
    public void setUp() {
        gregorianCalendar = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.CALENDAR);
        gregorianSpinners = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.SPINNERS);
        gregorianMonthYear = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.MONTH_YEAR);
        gregorianYear = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.YEAR);
        ethiopian = new DatePickerDetails(DatePickerDetails.DatePickerType.ETHIOPIAN, DatePickerDetails.DatePickerMode.SPINNERS);
        ethiopianMonthYear = new DatePickerDetails(DatePickerDetails.DatePickerType.ETHIOPIAN, DatePickerDetails.DatePickerMode.MONTH_YEAR);
        ethiopianYear = new DatePickerDetails(DatePickerDetails.DatePickerType.ETHIOPIAN, DatePickerDetails.DatePickerMode.YEAR);
    }

    @Test
    public void skipDaylightSavingGapIfExistsTest() {
        DateTimeZone originalDefaultTimeZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Africa/Nairobi")));

        // 1 Jan 1960 at 00:00:00 clocks were turned forward to 00:15:00
        LocalDateTime ldtOriginal = new LocalDateTime().withYear(1960).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        LocalDateTime ldtExpected = new LocalDateTime().withYear(1960).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0);

        assertEquals(ldtExpected, DateTimeUtils.skipDaylightSavingGapIfExists(ldtOriginal));
        DateTimeZone.setDefault(originalDefaultTimeZone);
    }

    @Test
    public void getDatePickerDetailsTest() {
        assertEquals(gregorianCalendar, DateTimeUtils.getDatePickerDetails(null));
        String appearance = "something";
        assertEquals(gregorianCalendar, DateTimeUtils.getDatePickerDetails(appearance));
        appearance = "no-calendar";
        assertEquals(gregorianSpinners, DateTimeUtils.getDatePickerDetails(appearance));
        appearance = "NO-CALENDAR";
        assertEquals(gregorianSpinners, DateTimeUtils.getDatePickerDetails(appearance));
        appearance = "month-year";
        assertEquals(gregorianMonthYear, DateTimeUtils.getDatePickerDetails(appearance));
        appearance = "MONTH-year";
        assertEquals(gregorianMonthYear, DateTimeUtils.getDatePickerDetails(appearance));
        appearance = "year";
        assertEquals(gregorianYear, DateTimeUtils.getDatePickerDetails(appearance));
        appearance = "Year";
        assertEquals(gregorianYear, DateTimeUtils.getDatePickerDetails(appearance));

        appearance = "ethiopian";
        assertEquals(ethiopian, DateTimeUtils.getDatePickerDetails(appearance));
        appearance = "Ethiopian month-year";
        assertEquals(ethiopianMonthYear, DateTimeUtils.getDatePickerDetails(appearance));
        appearance = "month-year ethiopian";
        assertEquals(ethiopianMonthYear, DateTimeUtils.getDatePickerDetails(appearance));
        appearance = "Ethiopian year";
        assertEquals(ethiopianYear, DateTimeUtils.getDatePickerDetails(appearance));
        appearance = "year ethiopian";
        assertEquals(ethiopianYear, DateTimeUtils.getDatePickerDetails(appearance));
    }
}
