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

    private Context context;

    @Before
    public void setUp() {
        gregorianDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.CALENDAR);
        ethiopianDatePickerDetails = new DatePickerDetails(DatePickerDetails.DatePickerType.ETHIOPIAN, DatePickerDetails.DatePickerMode.SPINNERS);

        context = Collect.getInstance();
    }

    @Test
    public void getDateTimeLabelTest() {
        long dateInMilliseconds = 687967200000L; // 20 Oct 1991 14:00
        Locale.setDefault(new Locale("af"));
        assertEquals("20 Okt 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 Okt 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("am"));
        assertEquals("20 ኦክተ 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 ኦክተ 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ar"));
        assertEquals("٢٠ أكتوبر، ١٩٩١", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("٢٠ أكتوبر، ١٩٩١ ١٤:٠٠", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("bn"));
        assertEquals("২০ অক্টোবর, ১৯৯১", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("২০ অক্টোবর, ১৯৯১ ১৪:০০", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ca"));
        assertEquals("20 oct. 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 oct. 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("cs"));
        assertEquals("20. 10. 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20. 10. 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("de"));
        assertEquals("20. Okt. 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20. Okt. 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("es"));
        assertEquals("20 de oct. de 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 de oct. de 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("es-rSV"));
        assertEquals("1991 Oct 20", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("1991 Oct 20 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("et"));
        assertEquals("20. okt 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20. okt 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("fa"));
        assertEquals("۲۰ اکتبر ۱۹۹۱", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("۲۰ اکتبر ۱۹۹۱،\u200F ۱۴:۰۰", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("fi"));
        assertEquals("20. lokakuuta 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20. lokakuuta 1991 14.00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("fr"));
        assertEquals("20 oct. 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 oct. 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ha"));
        assertEquals("20 Okt, 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 Okt, 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("hi"));
        assertEquals("20 अक्टू, 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 अक्टू, 1991, 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("hu"));
        assertEquals("1991. okt. 20.", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("1991. okt. 20. 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("in"));
        assertEquals("20 Okt 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 Okt 1991 14.00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("it"));
        assertEquals("20/ott/1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20/ott/1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("mg"));
        assertEquals("20 Okt 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 Okt 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ml"));
        assertEquals("1991 ഒക്ടോ 20", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("1991 ഒക്ടോ 20 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("mr"));
        assertEquals("२० ऑक्टो, १९९१", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("२० ऑक्टो, १९९१, १४:००", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("my"));
        assertEquals("၁၉၉၁ အောက်တိုဘာ ၂၀", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("၁၉၉၁ အောက်တိုဘာ ၂၀ ၁၄:၀၀", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("nb"));
        assertEquals("20. okt. 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20. okt. 1991, 14.00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ne-rNP"));
        assertEquals("1991 Oct 20", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("1991 Oct 20 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("nl"));
        assertEquals("20 okt. 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 okt. 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("no"));
        assertEquals("20. okt. 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20. okt. 1991, 14.00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("pl"));
        assertEquals("20 paź 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 paź 1991, 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ps"));
        assertEquals("۲۰ اکتوبر ۱۹۹۱", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("۲۰ اکتوبر ۱۹۹۱ ۱۴:۰۰", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("pt"));
        assertEquals("20 de out de 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 de out de 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ro"));
        assertEquals("20 oct. 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 oct. 1991, 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ru"));
        assertEquals("20 окт. 1991 г.", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 окт. 1991 г., 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("so"));
        assertEquals("20-Tob-1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20-Tob-1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("sq"));
        assertEquals("20 Tet 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 Tet 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("sv-rSE"));
        assertEquals("1991 Oct 20", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("1991 Oct 20 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("sw"));
        assertEquals("20 Okt 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 Okt 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("sw-rKE"));
        assertEquals("1991 Oct 20", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("1991 Oct 20 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ta"));
        assertEquals("20 அக்., 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 அக்., 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("th-eTH"));
        assertEquals("1991 Oct 20", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("1991 Oct 20 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ti"));
        assertEquals("20-ኦክተ-1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20-ኦክተ-1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("tl"));
        assertEquals("Okt 20, 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("Okt 20, 1991, 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("tl-rPH"));
        assertEquals("1991 Oct 20", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("1991 Oct 20 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("tr"));
        assertEquals("20 Eki 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 Eki 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("uk"));
        assertEquals("20 жовт. 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 жовт. 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ur"));
        assertEquals("20 اکتوبر، 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("20 اکتوبر، 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("ur-rPK"));
        assertEquals("1991 Oct 20", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("1991 Oct 20 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("vi"));
        assertEquals("20 thg 10, 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("14:00 20 thg 10, 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("zh"));
        assertEquals("1991年10月20日", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("1991年10月20日 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(new Locale("zu"));
        assertEquals("Okt 20, 1991", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, false, context));
        assertEquals("Okt 20, 1991 14:00", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), gregorianDatePickerDetails, true, context));

        Locale.setDefault(Locale.ENGLISH);
        assertEquals("9 Tikimt 1984 (Oct 20, 1991)", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), ethiopianDatePickerDetails, false, context));
        assertEquals("9 Tikimt 1984, 14:00 (Oct 20, 1991, 14:00)", DateTimeUtils.getDateTimeLabel(new Date(dateInMilliseconds), ethiopianDatePickerDetails, true, context));
    }
}
