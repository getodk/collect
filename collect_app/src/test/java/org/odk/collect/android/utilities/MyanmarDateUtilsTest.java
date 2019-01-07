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

        // 28/01/1900 ->
        LocalDateTime gregorianDateForConverting = new LocalDateTime()
                .withYear(1900)
                .withMonthOfYear(1)
                .withDayOfMonth(28);

        MyanmarDate myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1261, myanmarDate.getYearInt());
        assertEquals("Pyatho", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(29, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        int monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 15/02/1912 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(1912)
                .withMonthOfYear(2)
                .withDayOfMonth(15);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1273, myanmarDate.getYearInt());
        assertEquals("Tabodwe", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(28, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 07/03/1924 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(1924)
                .withMonthOfYear(3)
                .withDayOfMonth(7);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1285, myanmarDate.getYearInt());
        assertEquals("Tabaung", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(3, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 10/04/1938 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(1938)
                .withMonthOfYear(4)
                .withDayOfMonth(10);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1299, myanmarDate.getYearInt());
        //assertEquals("ှောင်းTagu", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(11, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 29/05/1944 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(1944)
                .withMonthOfYear(5)
                .withDayOfMonth(29);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1306, myanmarDate.getYearInt());
        assertEquals("Nayon", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(8, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 01/06/1959 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(1959)
                .withMonthOfYear(6)
                .withDayOfMonth(1);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1321, myanmarDate.getYearInt());
        assertEquals("Kason", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(26, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 11/07/1963 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(1963)
                .withMonthOfYear(7)
                .withDayOfMonth(11);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1325, myanmarDate.getYearInt());
        assertEquals("Waso", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(21, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 16/08/1972 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(1972)
                .withMonthOfYear(8)
                .withDayOfMonth(16);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1334, myanmarDate.getYearInt());
        assertEquals("Wagaung", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(7, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 20/09/1986 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(1986)
                .withMonthOfYear(9)
                .withDayOfMonth(20);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1348, myanmarDate.getYearInt());
        assertEquals("Tawthalin", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(17, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 20/10/1991 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(1991)
                .withMonthOfYear(10)
                .withDayOfMonth(20);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1353, myanmarDate.getYearInt());
        assertEquals("Thadingyut", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(12, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 25/11/2000 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(2000)
                .withMonthOfYear(11)
                .withDayOfMonth(25);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1362, myanmarDate.getYearInt());
        assertEquals("Tazaungmon", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(30, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 30/12/2013 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(2013)
                .withMonthOfYear(12)
                .withDayOfMonth(30);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1375, myanmarDate.getYearInt());
        assertEquals("Nadaw", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(28, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 02/01/2027 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(2027)
                .withMonthOfYear(1)
                .withDayOfMonth(2);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1388, myanmarDate.getYearInt());
        assertEquals("Nadaw", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(24, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 12/02/2033 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(2033)
                .withMonthOfYear(2)
                .withDayOfMonth(12);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1394, myanmarDate.getYearInt());
        assertEquals("Tabodwe", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(13, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 15/03/2048 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(2048)
                .withMonthOfYear(3)
                .withDayOfMonth(15);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1409, myanmarDate.getYearInt());
        //assertEquals("Late Tagu", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(2, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 21/04/2059 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(2059)
                .withMonthOfYear(4)
                .withDayOfMonth(21);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1421, myanmarDate.getYearInt());
        assertEquals("Tagu", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(9, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 24/05/2064 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(2064)
                .withMonthOfYear(5)
                .withDayOfMonth(24);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1426, myanmarDate.getYearInt());
        assertEquals("Nayon", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(10, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 04/06/2077 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(2077)
                .withMonthOfYear(6)
                .withDayOfMonth(4);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1439, myanmarDate.getYearInt());
        assertEquals("Nayon", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(14, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 19/07/2085 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(2085)
                .withMonthOfYear(7)
                .withDayOfMonth(19);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1447, myanmarDate.getYearInt());
        assertEquals("Waso", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
        assertEquals(28, myanmarDate.getMonthDay());

        assertGregorianDatesAreEqual(gregorianDateForConverting, MyanmarDateUtils.myanmarDateToGregorianDate(myanmarDate));
        monthIndex = MyanmarDateKernel.getMyanmarMonth(myanmarDate.getYearInt(), 1).getIndex().get(MyanmarDateUtils.getMonthId(myanmarDate));
        assertMyanmarDatesAreEqual(myanmarDate, MyanmarDateUtils.createMyanmarDate(myanmarDate.getYearInt(), monthIndex, myanmarDate.getMonthDay()));

        // 22/08/2097 ->
        gregorianDateForConverting = new LocalDateTime()
                .withYear(2097)
                .withMonthOfYear(8)
                .withDayOfMonth(22);

        myanmarDate = MyanmarDateUtils.gregorianDateToMyanmarDate(gregorianDateForConverting);
        assertEquals(1459, myanmarDate.getYearInt());
        assertEquals("Wagaung", myanmarDate.getMonthName(new LanguageCatalog(Language.ENGLISH)));
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
