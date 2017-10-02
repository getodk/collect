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

import android.app.DatePickerDialog;
import android.widget.DatePicker;

import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.widgets.DateTimeWidget;
import org.odk.collect.android.widgets.DateWidget;
import org.odk.collect.android.widgets.TimeWidget;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.TimeZone;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
/** https://github.com/opendatakit/collect/issues/356
 * Verify that the {@link DateWidget} and {@link DateTimeWidget} widget skips over
 * "daylight savings gaps".
 * This is needed on the day and time of a daylight savings transition because that date/time
 * doesn't exist.
 * In this test we set time to daylight saving gap and check if any crash occur*/
public class DaylightSavingTest {

    private static final String EAT_IME_ZONE = "Africa/Nairobi";
    private static final String CET_TIME_ZONE = "Europe/Warsaw";

    private TimeZone currentTimeZone;

    @Before
    public void setUp() {
        currentTimeZone = TimeZone.getDefault();
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(currentTimeZone);
    }

    @Test
    // 26 Mar 2017 at 02:00:00 clocks were turned forward to 03:00:00.
    public void testESTTimeZoneWithDateTimeWidget() {
        TimeZone.setDefault(TimeZone.getTimeZone(CET_TIME_ZONE));
        DateTimeWidget dateTimeWidget = prepareDateTimeWidget(2017, 3, 26, 2, 30);

        /*
         * We would get crash in this place using old approach {@link org.joda.time.DateTime} instead of
         * {@link org.joda.time.LocalDateTime}
         */
        dateTimeWidget.getAnswer();
    }

    @Test
    // 1 Jan 1960 at 00:00:00 clocks were turned forward to 00:15:00
    public void testEATTimezoneWithDateWidget() {
        TimeZone.setDefault(TimeZone.getTimeZone(EAT_IME_ZONE));
        DateWidget dateWidget = prepareDateWidget(1960, 0, 1);

        /*
         * We would get crash in this place using old approach {@link org.joda.time.DateTime} instead of
         * {@link org.joda.time.LocalDateTime}
         */
        dateWidget.getAnswer();
    }

    private DateWidget prepareDateWidget(int year, int month, int day) {
        QuestionDef questionDefStub = mock(QuestionDef.class);
        IFormElement iformElementStub = mock(IFormElement.class);
        FormEntryPrompt formEntryPromptStub = mock(FormEntryPrompt.class);

        when(iformElementStub.getAdditionalAttribute(anyString(), anyString())).thenReturn(null);
        when(formEntryPromptStub.getQuestion()).thenReturn(questionDefStub);
        when(formEntryPromptStub.getFormElement()).thenReturn(iformElementStub);
        when(formEntryPromptStub.getQuestion().getAppearanceAttr()).thenReturn("no-calendar");

        DatePickerDialog datePickerDialog = mock(DatePickerDialog.class);
        DatePicker datePicker = mock(DatePicker.class);
        when(datePickerDialog.getDatePicker()).thenReturn(datePicker);
        when(datePickerDialog.getDatePicker().getYear()).thenReturn(year);
        when(datePickerDialog.getDatePicker().getMonth()).thenReturn(month);
        when(datePickerDialog.getDatePicker().getDayOfMonth()).thenReturn(day);

        DateWidget dateWidget = new DateWidget(RuntimeEnvironment.application, formEntryPromptStub);
        dateWidget.setDatePickerDialog(datePickerDialog);
        return dateWidget;
    }

    private DateTimeWidget prepareDateTimeWidget(int year, int month, int day, int hour, int minute) {
        QuestionDef questionDefStub = mock(QuestionDef.class);
        IFormElement iformElementStub = mock(IFormElement.class);
        FormEntryPrompt formEntryPromptStub = mock(FormEntryPrompt.class);

        when(iformElementStub.getAdditionalAttribute(anyString(), anyString())).thenReturn(null);
        when(formEntryPromptStub.getQuestion()).thenReturn(questionDefStub);
        when(formEntryPromptStub.getFormElement()).thenReturn(iformElementStub);
        when(formEntryPromptStub.getQuestion().getAppearanceAttr()).thenReturn("no-calendar");

        DateWidget dateWidget = mock(DateWidget.class);
        when(dateWidget.getDate()).thenReturn(new LocalDateTime().withYear(year).withMonthOfYear(month).withDayOfMonth(day));

        TimeWidget timeWidget = mock(TimeWidget.class);
        when(timeWidget.getHour()).thenReturn(hour);
        when(timeWidget.getMinute()).thenReturn(minute);

        DateTimeWidget dateTimeWidget = new DateTimeWidget(RuntimeEnvironment.application, formEntryPromptStub);
        dateTimeWidget.setDateWidget(dateWidget);
        dateTimeWidget.setTimeWidget(timeWidget);

        return dateTimeWidget;
    }
}