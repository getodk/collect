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

import org.junit.Test;

import bikramsambat.BikramSambatDate;
import bikramsambat.BsCalendar;
import bikramsambat.BsException;
import bikramsambat.BsGregorianDate;

import static org.junit.Assert.assertEquals;

// Results confirmed with https://www.ashesh.com.np/nepali-date-converter.php
public class BikramSambatTest {

    @Test
    public void convertingGregorianToBikramSambatTest() throws BsException {
        BikramSambatDate bikramSambatDate = BsCalendar.getInstance().toBik(1914, 1, 3);
        assertEquals(1970, bikramSambatDate.year);
        assertEquals(9, bikramSambatDate.month);
        assertEquals(20, bikramSambatDate.day);

        bikramSambatDate = BsCalendar.getInstance().toBik(1923, 2, 6);
        assertEquals(1979, bikramSambatDate.year);
        assertEquals(10, bikramSambatDate.month);
        assertEquals(24, bikramSambatDate.day);

        bikramSambatDate = BsCalendar.getInstance().toBik(1935, 3, 9);
        assertEquals(1991, bikramSambatDate.year);
        assertEquals(11, bikramSambatDate.month);
        assertEquals(26, bikramSambatDate.day);

        bikramSambatDate = BsCalendar.getInstance().toBik(1949, 4, 12);
        assertEquals(2005, bikramSambatDate.year);
        assertEquals(12, bikramSambatDate.month);
        assertEquals(30, bikramSambatDate.day);

        bikramSambatDate = BsCalendar.getInstance().toBik(1951, 5, 15);
        assertEquals(2008, bikramSambatDate.year);
        assertEquals(2, bikramSambatDate.month);
        assertEquals(1, bikramSambatDate.day);

        bikramSambatDate = BsCalendar.getInstance().toBik(1968, 6, 18);
        assertEquals(2025, bikramSambatDate.year);
        assertEquals(3, bikramSambatDate.month);
        assertEquals(5, bikramSambatDate.day);

        bikramSambatDate = BsCalendar.getInstance().toBik(1976, 7, 21);
        assertEquals(2033, bikramSambatDate.year);
        assertEquals(4, bikramSambatDate.month);
        assertEquals(6, bikramSambatDate.day);

        bikramSambatDate = BsCalendar.getInstance().toBik(1985, 8, 24);
        assertEquals(2042, bikramSambatDate.year);
        assertEquals(5, bikramSambatDate.month);
        assertEquals(8, bikramSambatDate.day);

        bikramSambatDate = BsCalendar.getInstance().toBik(1992, 9, 27);
        assertEquals(2049, bikramSambatDate.year);
        assertEquals(6, bikramSambatDate.month);
        assertEquals(11, bikramSambatDate.day);

        bikramSambatDate = BsCalendar.getInstance().toBik(2003, 10, 29);
        assertEquals(2060, bikramSambatDate.year);
        assertEquals(7, bikramSambatDate.month);
        assertEquals(12, bikramSambatDate.day);

        bikramSambatDate = BsCalendar.getInstance().toBik(2019, 11, 17);
        assertEquals(2076, bikramSambatDate.year);
        assertEquals(8, bikramSambatDate.month);
        assertEquals(1, bikramSambatDate.day);
    }

    @Test
    public void convertingBikramSambatToGregorian() throws BsException {
        BsGregorianDate bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(1972, 1, 1));
        assertEquals(1915, bsGregorianDate.year);
        assertEquals(4, bsGregorianDate.month);
        assertEquals(13, bsGregorianDate.day);

        bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(1983, 2, 5));
        assertEquals(1926, bsGregorianDate.year);
        assertEquals(5, bsGregorianDate.month);
        assertEquals(18, bsGregorianDate.day);

        bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(1994, 3, 10));
        assertEquals(1937, bsGregorianDate.year);
        assertEquals(6, bsGregorianDate.month);
        assertEquals(23, bsGregorianDate.day);

        bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(2005, 4, 15));
        assertEquals(1948, bsGregorianDate.year);
        assertEquals(7, bsGregorianDate.month);
        assertEquals(30, bsGregorianDate.day);

        bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(2016, 5, 20));
        assertEquals(1959, bsGregorianDate.year);
        assertEquals(9, bsGregorianDate.month);
        assertEquals(5, bsGregorianDate.day);

        bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(2027, 6, 25));
        assertEquals(1970, bsGregorianDate.year);
        assertEquals(10, bsGregorianDate.month);
        assertEquals(11, bsGregorianDate.day);

        bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(2038, 7, 28));
        assertEquals(1981, bsGregorianDate.year);
        assertEquals(11, bsGregorianDate.month);
        assertEquals(13, bsGregorianDate.day);

        bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(2049, 8, 17));
        assertEquals(1992, bsGregorianDate.year);
        assertEquals(12, bsGregorianDate.month);
        assertEquals(2, bsGregorianDate.day);

        bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(2051, 9, 6));
        assertEquals(1994, bsGregorianDate.year);
        assertEquals(12, bsGregorianDate.month);
        assertEquals(21, bsGregorianDate.day);

        bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(2062, 10, 4));
        assertEquals(2006, bsGregorianDate.year);
        assertEquals(1, bsGregorianDate.month);
        assertEquals(17, bsGregorianDate.day);

        bsGregorianDate = BsCalendar.getInstance().toGreg(new BikramSambatDate(2073, 11, 14));
        assertEquals(2017, bsGregorianDate.year);
        assertEquals(2, bsGregorianDate.month);
        assertEquals(25, bsGregorianDate.day);
    }
}
