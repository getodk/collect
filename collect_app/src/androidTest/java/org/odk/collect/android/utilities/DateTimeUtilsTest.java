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
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.DatePickerDetails;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DateTimeUtilsTest {

    private DatePickerDetails gregorianDatePickerDetails;
    private DatePickerDetails ethiopianDatePickerDetails;
    private DatePickerDetails copticDatePickerDetails;
    private DatePickerDetails islamicDatePickerDetails;
    private DatePickerDetails bikramSambatDatePickerDetails;

    private Context context;
    private Locale defaultLocale;
    private TimeZone defaultTimezone;

    @Before
    public void setUp() {
        gregorianDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.CALENDAR);
        ethiopianDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.ETHIOPIAN, DatePickerDetails.DatePickerMode.SPINNERS);
        copticDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.COPTIC, DatePickerDetails.DatePickerMode.SPINNERS);
        islamicDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.ISLAMIC, DatePickerDetails.DatePickerMode.SPINNERS);
        bikramSambatDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.BIKRAM_SAMBAT, DatePickerDetails.DatePickerMode.SPINNERS);

        context = Collect.getInstance();
        defaultLocale = Locale.getDefault();
        defaultTimezone = TimeZone.getDefault();
    }

    @Test
    public void getDateTimeLabelTest() {
        Locale.setDefault(Locale.ENGLISH);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // 20 Oct 1991 14:00 GMT
        Calendar calendar = Calendar.getInstance();
        calendar.set(1991, 9, 20, 14, 0, 0);
        Date date = calendar.getTime();

        assertEquals("Oct 20, 1991", DateTimeUtils.getDateTimeLabel(date, gregorianDatePickerDetails, false, context));
        assertEquals("Oct 20, 1991, 14:00", DateTimeUtils.getDateTimeLabel(date, gregorianDatePickerDetails, true, context));

        assertEquals("9 Tikimt 1984 (Oct 20, 1991)", DateTimeUtils.getDateTimeLabel(date, ethiopianDatePickerDetails, false, context));
        assertEquals("9 Tikimt 1984, 14:00 (Oct 20, 1991, 14:00)", DateTimeUtils.getDateTimeLabel(date, ethiopianDatePickerDetails, true, context));

        assertEquals("9 Paopi 1708 (Oct 20, 1991)", DateTimeUtils.getDateTimeLabel(date, copticDatePickerDetails, false, context));
        assertEquals("9 Paopi 1708, 14:00 (Oct 20, 1991, 14:00)", DateTimeUtils.getDateTimeLabel(date, copticDatePickerDetails, true, context));

        assertEquals("11 Rabi' al-thani 1412 (Oct 20, 1991)", DateTimeUtils.getDateTimeLabel(date, islamicDatePickerDetails, false, context));
        assertEquals("11 Rabi' al-thani 1412, 14:00 (Oct 20, 1991, 14:00)", DateTimeUtils.getDateTimeLabel(date, islamicDatePickerDetails, true, context));

        assertEquals("3 कार्तिक 2048 (Oct 20, 1991)", DateTimeUtils.getDateTimeLabel(date, bikramSambatDatePickerDetails, false, context));
        assertEquals("3 कार्तिक 2048, 14:00 (Oct 20, 1991, 14:00)", DateTimeUtils.getDateTimeLabel(date, bikramSambatDatePickerDetails, true, context));
    }

    @After
    public void resetTimeZone() {
        Locale.setDefault(defaultLocale);
        TimeZone.setDefault(defaultTimezone);
    }
}
