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

import android.content.Context;
import android.support.test.filters.Suppress;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.DatePickerDetails;

import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DateTimeUtilsTest {

    private DatePickerDetails gregorianDatePickerDetails;
    private DatePickerDetails ethiopianDatePickerDetails;
    private DatePickerDetails copticDatePickerDetails;
    private DatePickerDetails islamicDatePickerDetails;

    private Context context;

    @Before
    public void setUp() {
        gregorianDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.CALENDAR);
        ethiopianDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.ETHIOPIAN, DatePickerDetails.DatePickerMode.SPINNERS);
        copticDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.COPTIC, DatePickerDetails.DatePickerMode.SPINNERS);
        islamicDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.ISLAMIC, DatePickerDetails.DatePickerMode.SPINNERS);

        context = Collect.getInstance();
    }

    @Test
    @Suppress
    public void getDateTimeLabelTest() {
        long dateInMilliseconds = 687967200000L; // 20 Oct 1991 14:00

        Locale.setDefault(Locale.ENGLISH);
        assertEquals("Oct 20, 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("Oct 20, 1991, 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(Locale.ENGLISH);
        assertEquals("9 Tikimt 1984 (Oct 20, 1991)", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), ethiopianDatePickerDetails, false, context));
        assertEquals("9 Tikimt 1984, 14:00 (Oct 20, 1991, 14:00)", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), ethiopianDatePickerDetails, true, context));

        Locale.setDefault(Locale.ENGLISH);
        assertEquals("9 Paopi 1708 (Oct 20, 1991)", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), copticDatePickerDetails, false, context));
        assertEquals("9 Paopi 1708, 14:00 (Oct 20, 1991, 14:00)", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), copticDatePickerDetails, true, context));

        Locale.setDefault(Locale.ENGLISH);
        assertEquals("11 Rabī‘ ath-thānī 1412 (Oct 20, 1991)", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), islamicDatePickerDetails, false, context));
        assertEquals("11 Rabī‘ ath-thānī 1412, 14:00 (Oct 20, 1991, 14:00)", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), islamicDatePickerDetails, true, context));
    }
}
