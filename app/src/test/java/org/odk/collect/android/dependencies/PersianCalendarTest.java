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

package org.odk.collect.android.dependencies;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.PersianChronologyKhayyamBorkowski;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Results confirmed with:
 * http://www.iranchamber.com/calendar/converter/iranian_calendar_converter.php
 * and
 * https://calcuworld.com/calendar-calculators/persian-calendar-converter/
 */
public class PersianCalendarTest {

    @Test
    public void convertingGregorianToPersianTest() {
        DateTime gregorianDateTime = new DateTime().withYear(1905).withMonthOfYear(2).withDayOfMonth(5);
        assertDate(getPersianDateTime(gregorianDateTime), 1283, 11, 16);

        gregorianDateTime = new DateTime().withYear(1918).withMonthOfYear(12).withDayOfMonth(1);
        assertDate(getPersianDateTime(gregorianDateTime), 1297, 9, 9);

        gregorianDateTime = new DateTime().withYear(1921).withMonthOfYear(1).withDayOfMonth(31);
        assertDate(getPersianDateTime(gregorianDateTime), 1299, 11, 11);

        gregorianDateTime = new DateTime().withYear(1935).withMonthOfYear(5).withDayOfMonth(18);
        assertDate(getPersianDateTime(gregorianDateTime), 1314, 2, 27);

        gregorianDateTime = new DateTime().withYear(1947).withMonthOfYear(9).withDayOfMonth(24);
        assertDate(getPersianDateTime(gregorianDateTime), 1326, 7, 1);

        gregorianDateTime = new DateTime().withYear(1952).withMonthOfYear(7).withDayOfMonth(17);
        assertDate(getPersianDateTime(gregorianDateTime), 1331, 4, 26);

        gregorianDateTime = new DateTime().withYear(1968).withMonthOfYear(10).withDayOfMonth(8);
        assertDate(getPersianDateTime(gregorianDateTime), 1347, 7, 16);

        gregorianDateTime = new DateTime().withYear(1970).withMonthOfYear(4).withDayOfMonth(30);
        assertDate(getPersianDateTime(gregorianDateTime), 1349, 2, 10);

        gregorianDateTime = new DateTime().withYear(1987).withMonthOfYear(6).withDayOfMonth(8);
        assertDate(getPersianDateTime(gregorianDateTime), 1366, 3, 18);

        gregorianDateTime = new DateTime().withYear(1991).withMonthOfYear(10).withDayOfMonth(20);
        assertDate(getPersianDateTime(gregorianDateTime), 1370, 7, 28);

        gregorianDateTime = new DateTime().withYear(2005).withMonthOfYear(11).withDayOfMonth(17);
        assertDate(getPersianDateTime(gregorianDateTime), 1384, 8, 26);

        gregorianDateTime = new DateTime().withYear(2019).withMonthOfYear(6).withDayOfMonth(5);
        assertDate(getPersianDateTime(gregorianDateTime), 1398, 3, 15);

        gregorianDateTime = new DateTime().withYear(2023).withMonthOfYear(2).withDayOfMonth(28);
        assertDate(getPersianDateTime(gregorianDateTime), 1401, 12, 9);

        gregorianDateTime = new DateTime().withYear(2034).withMonthOfYear(4).withDayOfMonth(1);
        assertDate(getPersianDateTime(gregorianDateTime), 1413, 1, 12);

        gregorianDateTime = new DateTime().withYear(2048).withMonthOfYear(1).withDayOfMonth(2);
        assertDate(getPersianDateTime(gregorianDateTime), 1426, 10, 12);

        gregorianDateTime = new DateTime().withYear(2056).withMonthOfYear(8).withDayOfMonth(22);
        assertDate(getPersianDateTime(gregorianDateTime), 1435, 6, 1);

        gregorianDateTime = new DateTime().withYear(2063).withMonthOfYear(10).withDayOfMonth(11);
        assertDate(getPersianDateTime(gregorianDateTime), 1442, 7, 19);

        gregorianDateTime = new DateTime().withYear(2075).withMonthOfYear(4).withDayOfMonth(18);
        assertDate(getPersianDateTime(gregorianDateTime), 1454, 1, 29);

        gregorianDateTime = new DateTime().withYear(2080).withMonthOfYear(12).withDayOfMonth(30);
        assertDate(getPersianDateTime(gregorianDateTime), 1459, 10, 10);

        gregorianDateTime = new DateTime().withYear(2099).withMonthOfYear(7).withDayOfMonth(14);
        assertDate(getPersianDateTime(gregorianDateTime), 1478, 4, 24);
    }

    @Test
    public void convertingPersianToGregorianTest() {
        DateTime persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1281).withMonthOfYear(3).withDayOfMonth(17);
        assertDate(getGregorianDateTime(persianDateTime), 1902, 6, 8);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1290).withMonthOfYear(1).withDayOfMonth(30);
        assertDate(getGregorianDateTime(persianDateTime), 1911, 4, 20);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1307).withMonthOfYear(10).withDayOfMonth(1);
        assertDate(getGregorianDateTime(persianDateTime), 1928, 12, 22);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1314).withMonthOfYear(7).withDayOfMonth(14);
        assertDate(getGregorianDateTime(persianDateTime), 1935, 10, 7);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1326).withMonthOfYear(11).withDayOfMonth(21);
        assertDate(getGregorianDateTime(persianDateTime), 1948, 2, 11);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1339).withMonthOfYear(3).withDayOfMonth(28);
        assertDate(getGregorianDateTime(persianDateTime), 1960, 6, 18);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1348).withMonthOfYear(5).withDayOfMonth(9);
        assertDate(getGregorianDateTime(persianDateTime), 1969, 7, 31);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1352).withMonthOfYear(8).withDayOfMonth(5);
        assertDate(getGregorianDateTime(persianDateTime), 1973, 10, 27);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1367).withMonthOfYear(12).withDayOfMonth(15);
        assertDate(getGregorianDateTime(persianDateTime), 1989, 3, 6);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1375).withMonthOfYear(2).withDayOfMonth(20);
        assertDate(getGregorianDateTime(persianDateTime), 1996, 5, 9);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1381).withMonthOfYear(6).withDayOfMonth(11);
        assertDate(getGregorianDateTime(persianDateTime), 2002, 9, 2);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1395).withMonthOfYear(9).withDayOfMonth(29);
        assertDate(getGregorianDateTime(persianDateTime), 2016, 12, 19);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1400).withMonthOfYear(5).withDayOfMonth(10);
        assertDate(getGregorianDateTime(persianDateTime), 2021, 8, 1);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1417).withMonthOfYear(10).withDayOfMonth(12);
        assertDate(getGregorianDateTime(persianDateTime), 2039, 1, 2);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1424).withMonthOfYear(8).withDayOfMonth(20);
        assertDate(getGregorianDateTime(persianDateTime), 2045, 11, 10);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1439).withMonthOfYear(11).withDayOfMonth(5);
        assertDate(getGregorianDateTime(persianDateTime), 2061, 1, 24);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1443).withMonthOfYear(12).withDayOfMonth(17);
        assertDate(getGregorianDateTime(persianDateTime), 2065, 3, 7);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1452).withMonthOfYear(3).withDayOfMonth(4);
        assertDate(getGregorianDateTime(persianDateTime), 2073, 5, 24);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1467).withMonthOfYear(9).withDayOfMonth(22);
        assertDate(getGregorianDateTime(persianDateTime), 2088, 12, 12);

        persianDateTime = new DateTime(PersianChronologyKhayyamBorkowski.getInstance()).withYear(1474).withMonthOfYear(6).withDayOfMonth(12);
        assertDate(getGregorianDateTime(persianDateTime), 2095, 9, 2);
    }

    private void assertDate(DateTime dateTime, int expectedYear, int expectedMonth, int expectedDay) {
        assertEquals(expectedYear, dateTime.getYear());
        assertEquals(expectedMonth, dateTime.getMonthOfYear());
        assertEquals(expectedDay, dateTime.getDayOfMonth());
    }

    private DateTime getPersianDateTime(DateTime gregorianDateTime) {
        return new DateTime(gregorianDateTime).withChronology(PersianChronologyKhayyamBorkowski.getInstance());
    }

    private DateTime getGregorianDateTime(DateTime persianDateTime) {
        return new DateTime(persianDateTime).withChronology(GregorianChronology.getInstance());
    }
}
