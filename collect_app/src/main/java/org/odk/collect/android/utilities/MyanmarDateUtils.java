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

import org.joda.time.LocalDateTime;

import mmcalendar.Language;
import mmcalendar.LanguageCatalog;
import mmcalendar.MyanmarDate;
import mmcalendar.MyanmarDateConverter;
import mmcalendar.MyanmarDateKernel;
import mmcalendar.ThingyanCalculator;
import mmcalendar.WesternDate;
import mmcalendar.WesternDateConverter;

public final class MyanmarDateUtils {

    private MyanmarDateUtils() {
    }

    public static MyanmarDate gregorianDateToMyanmarDate(LocalDateTime localDateTime) {
        return MyanmarDateConverter.convert(
                localDateTime.getYear(),
                localDateTime.getMonthOfYear(),
                localDateTime.getDayOfMonth(),
                localDateTime.getHourOfDay(),
                localDateTime.getMinuteOfHour(),
                localDateTime.getSecondOfMinute());
    }

    public static LocalDateTime myanmarDateToGregorianDate(MyanmarDate myanmarDate) {
        WesternDate westernDate = WesternDateConverter.convert(myanmarDate);
        return new LocalDateTime()
                .withYear(westernDate.getYear())
                .withMonthOfYear(westernDate.getMonth())
                .withDayOfMonth(westernDate.getDay())
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
    }

    public static MyanmarDate createMyanmarDate(int myanmarYear, int myanmarMonthIndex, int myanmarMonthDay) {
        return MyanmarDateKernel.j2m(MyanmarDateKernel.m2j(myanmarYear, myanmarMonthIndex, myanmarMonthDay));
    }

    public static String[] getMyanmarMonthsArray(int myanmarYear) {
        return MyanmarDateKernel
                .getMyanmarMonth(myanmarYear, 1)
                .getMonthNameList(new LanguageCatalog(Language.MYANMAR))
                .toArray(new String[0]);
    }

    public static int getFirstMonthDay(MyanmarDate myanmarDate) {
        return isFirstYearMonth(myanmarDate) ? getNewYearsDay(myanmarDate.getYearInt()) : 1;
    }

    private static int getFirstMonthDay(int myanmarYear, int monthIndex) {
        return isFirstYearMonth(myanmarYear, monthIndex) ? getNewYearsDay(myanmarYear) : 1;
    }

    public static int getMonthId(MyanmarDate myanmarDate) {
        return MyanmarDateKernel
                .getMyanmarMonth(myanmarDate.getYearInt(), 1)
                .getMonthNameList(new LanguageCatalog(Language.MYANMAR))
                .indexOf(myanmarDate.getMonthName(new LanguageCatalog(Language.MYANMAR)));
    }

    public static int getMonthLength(MyanmarDate myanmarDate) {
        int newYearsDayOfNextYear = getNewYearsDay(myanmarDate.getYearInt() + 1);
        return isLastMonthInYear(myanmarDate) && newYearsDayOfNextYear > 1
                ? newYearsDayOfNextYear - 1
                : myanmarDate.getMonthLength();
    }

    public static int getMonthLength(int myanmarYear, int monthIndex) {
        MyanmarDate myanmarDate = MyanmarDateUtils.createMyanmarDate(myanmarYear, monthIndex, MyanmarDateUtils.getFirstMonthDay(myanmarYear, monthIndex));
        return getMonthLength(myanmarDate);
    }

    private static int getNewYearsDay(int myanmarYear) {
        return MyanmarDateKernel
                .j2m(ThingyanCalculator.getThingyan(myanmarYear).getMyanmarNewYearDay())
                .getMonthDay();
    }

    private static boolean isLastMonthInYear(MyanmarDate myanmarDate) {
        return getMonthId(myanmarDate) == getMyanmarMonthsArray(myanmarDate.getYearInt()).length - 1;
    }

    private static boolean isFirstYearMonth(MyanmarDate myanmarDate) {
        return getMonthId(myanmarDate) == 0;
    }

    private static boolean isFirstYearMonth(int myanmarYear, int monthIndex) {
        return monthIndex == MyanmarDateKernel.getMyanmarMonth(myanmarYear, 1).getIndex().get(0);
    }
}
