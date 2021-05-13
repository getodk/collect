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

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.data.TimeData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.GregorianChronology;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DateTimeUtilsTest {
    private final LocalDateTime date = new LocalDateTime().withDate(2010, 5, 12);
    private final LocalDateTime time = new LocalDateTime().withTime(12, 10, 0, 0);

    @Test
    public void getCurrentDateTime_returnsCurrentDateAndTimeData() {
        LocalDateTime localDateTime = new LocalDateTime()
                .withDate(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth())
                .withTime(DateTime.now().getHourOfDay(), DateTime.now().getMinuteOfHour(), 0, 0);
        assertEquals(DateTimeUtils.getCurrentDateTime(), localDateTime);
    }

    @Test
    public void getSelectedDate_returnsCorrectDateAndTimeData() {
        LocalDateTime localDateTime = new LocalDateTime()
                .withDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth())
                .withTime(time.getHourOfDay(), time.getMinuteOfHour(), 0, 0);
        assertEquals(DateTimeUtils.getSelectedDate(date, time), localDateTime);
    }

    @Test
    public void getDateAsGregorian_returnsCorrectDateAndTimeData() {
        LocalDateTime localDateTime = DateTimeUtils.skipDaylightSavingGapIfExists(date)
                .toDateTime()
                .withChronology(GregorianChronology.getInstance())
                .toLocalDateTime();
        assertEquals(DateTimeUtils.getDateAsGregorian(date), localDateTime);
    }

    @Test
    public void getSelectedTime_returnsCorrectDateAndTimeData() {
        LocalDateTime localDateTime = new LocalDateTime()
                .withDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth())
                .withTime(time.getHourOfDay(), time.getMinuteOfHour(), 0, 0);
        assertEquals(DateTimeUtils.getSelectedTime(time, date), localDateTime);
    }

    @Test
    public void getTimeData_returnsCorrectTime() {
        assertEquals(DateTimeUtils.getTimeData(time.toDateTime()).getDisplayText(), new TimeData(time.toDate()).getDisplayText());
    }

    @Test
    public void getDateWithSkippedDaylightSavingGapIfExists_returnsCorrectDateAndTimeData() {
        LocalDateTime localDateTime = DateTimeUtils.skipDaylightSavingGapIfExists(date)
                .toDateTime()
                .toLocalDateTime();
        assertEquals(DateTimeUtils.getDateWithSkippedDaylightSavingGapIfExists(date), localDateTime);
    }

    @Test
    public void skipDaylightSavingGapIfExistsTest() {
        DateTimeZone originalDefaultTimeZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.forID("Europe/Warsaw"));

        // 29 March 2020 at 02:00:00 clocks were turned forward to 03:00:00
        LocalDateTime ldtOriginal = new LocalDateTime().withYear(2020).withMonthOfYear(3).withDayOfMonth(29).withHourOfDay(2).withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
        LocalDateTime ldtExpected = new LocalDateTime().withYear(2020).withMonthOfYear(3).withDayOfMonth(29).withHourOfDay(3).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

        assertEquals(ldtExpected, DateTimeUtils.skipDaylightSavingGapIfExists(ldtOriginal));
        DateTimeZone.setDefault(originalDefaultTimeZone);
    }
}
