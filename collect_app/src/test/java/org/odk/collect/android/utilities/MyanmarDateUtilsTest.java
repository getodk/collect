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

package org.odk.collect.android.utilities;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.tz.UTCProvider;
import org.junit.Test;

import mmcalendar.Language;
import mmcalendar.LanguageCatalog;
import mmcalendar.MyanmarDate;
import mmcalendar.MyanmarDateKernel;

import static org.junit.Assert.assertEquals;

// Results confirmed with https://yan9a.github.io/mcal/
public class MyanmarDateUtilsTest {

    @Test
    public void convertDatesTest() {
        DateTimeZone.setProvider(new UTCProvider());

        LocalDateTime gregorianDateForConverting = new LocalDateTime()
                .withYear(1900)
                .withMonthOfYear(1)
                .withDayOfMonth(28);

        MyanmarDate myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1261, myanmarDate.getYearInt());
        assertEquals("ပြာသို", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(29, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        int monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(1912)
                .withMonthOfYear(2)
                .withDayOfMonth(15);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1273, myanmarDate.getYearInt());
        assertEquals("တပို့တွဲ", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(28, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(1924)
                .withMonthOfYear(3)
                .withDayOfMonth(7);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1285, myanmarDate.getYearInt());
        assertEquals("တပေါင်း", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(3, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(1938)
                .withMonthOfYear(4)
                .withDayOfMonth(10);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1299, myanmarDate.getYearInt());
        assertEquals("နှောင်းတန်ခူး", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(11, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(1944)
                .withMonthOfYear(5)
                .withDayOfMonth(29);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1306, myanmarDate.getYearInt());
        assertEquals("နယုန်", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(8, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(1959)
                .withMonthOfYear(6)
                .withDayOfMonth(1);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1321, myanmarDate.getYearInt());
        assertEquals("ကဆုန်", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(26, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(1963)
                .withMonthOfYear(7)
                .withDayOfMonth(11);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1325, myanmarDate.getYearInt());
        assertEquals("ဝါဆို", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(21, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(1972)
                .withMonthOfYear(8)
                .withDayOfMonth(16);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1334, myanmarDate.getYearInt());
        assertEquals("ဝါခေါင်", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(7, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(1986)
                .withMonthOfYear(9)
                .withDayOfMonth(20);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1348, myanmarDate.getYearInt());
        assertEquals("တော်သလင်း", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(17, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(1991)
                .withMonthOfYear(10)
                .withDayOfMonth(20);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1353, myanmarDate.getYearInt());
        assertEquals("သီတင်းကျွတ်", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(12, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(2000)
                .withMonthOfYear(11)
                .withDayOfMonth(25);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1362, myanmarDate.getYearInt());
        assertEquals("တန်ဆောင်မုန်း", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(30, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(2013)
                .withMonthOfYear(12)
                .withDayOfMonth(30);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1375, myanmarDate.getYearInt());
        assertEquals("နတ်တော်", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(28, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(2027)
                .withMonthOfYear(1)
                .withDayOfMonth(2);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1388, myanmarDate.getYearInt());
        assertEquals("နတ်တော်", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(24, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(2033)
                .withMonthOfYear(2)
                .withDayOfMonth(12);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1394, myanmarDate.getYearInt());
        assertEquals("တပို့တွဲ", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(13, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(2048)
                .withMonthOfYear(3)
                .withDayOfMonth(15);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1409, myanmarDate.getYearInt());
        assertEquals("နှောင်းတန်ခူး", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(2, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(2059)
                .withMonthOfYear(4)
                .withDayOfMonth(21);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1421, myanmarDate.getYearInt());
        assertEquals("တန်ခူး", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(9, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(2064)
                .withMonthOfYear(5)
                .withDayOfMonth(24);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1426, myanmarDate.getYearInt());
        assertEquals("နယုန်", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(10, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(2077)
                .withMonthOfYear(6)
                .withDayOfMonth(4);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1439, myanmarDate.getYearInt());
        assertEquals("နယုန်", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(14, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(2085)
                .withMonthOfYear(7)
                .withDayOfMonth(19);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1447, myanmarDate.getYearInt());
        assertEquals("ဝါဆို", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(28, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        gregorianDateForConverting = new LocalDateTime()
                .withYear(2097)
                .withMonthOfYear(8)
                .withDayOfMonth(22);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1459, myanmarDate.getYearInt());
        assertEquals("ဝါခေါင်", myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
        assertEquals(15, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));
    }

    private void assertGregorianDatesAreEqual(LocalDateTime firstDate, LocalDateTime secondDate) {
        assertEquals(firstDate.getYear(), secondDate.getYear());
        assertEquals(firstDate.getMonthOfYear(), secondDate.getMonthOfYear());
        assertEquals(firstDate.getDayOfMonth(), secondDate.getDayOfMonth());
    }

    private void assertMyanmarDatesAreEqual(MyanmarDate firstDate, MyanmarDate secondDate) {
        assertEquals(firstDate.getYearInt(), secondDate.getYearInt());
        assertEquals(firstDate.getMonth(), secondDate.getMonth());
        assertEquals(firstDate.getYearInt(), secondDate.getYearInt());
    }

}
