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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
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
import static org.mockito.Mockito.stub;

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

    private TimeZone mCurrentTimeZone;

    @Before
    public void setUp() {
        mCurrentTimeZone = TimeZone.getDefault();
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(mCurrentTimeZone);
    }

    @Test
    // 26 Mar 2017 at 02:00:00 clocks were turned forward to 03:00:00.
    public void testESTTimeZoneWithDateTimeWidget() {
        TimeZone.setDefault(TimeZone.getTimeZone(CET_TIME_ZONE));
        DateTimeWidget dateTimeWidget = prepareDateTimeWidget(2017, 3, 26, 2, 30);

        /**
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

        /**
         * We would get crash in this place using old approach {@link org.joda.time.DateTime} instead of
         * {@link org.joda.time.LocalDateTime}
         */
        dateWidget.getAnswer();
    }

    private DateWidget prepareDateWidget(int year, int month, int day) {
        QuestionDef questionDefStub = mock(QuestionDef.class);
        IFormElement iFormElementStub = mock(IFormElement.class);
        FormEntryPrompt formEntryPromptStub = mock(FormEntryPrompt.class);

        stub(iFormElementStub.getAdditionalAttribute(anyString(), anyString())).toReturn(null);
        stub(formEntryPromptStub.getQuestion()).toReturn(questionDefStub);
        stub(formEntryPromptStub.getFormElement()).toReturn(iFormElementStub);
        stub(formEntryPromptStub.getQuestion().getAppearanceAttr()).toReturn("no-calendar");

        DatePickerDialog datePickerDialog = mock(DatePickerDialog.class);
        DatePicker datePicker = mock(DatePicker.class);
        stub(datePickerDialog.getDatePicker()).toReturn(datePicker);
        stub(datePickerDialog.getDatePicker().getYear()).toReturn(year);
        stub(datePickerDialog.getDatePicker().getMonth()).toReturn(month);
        stub(datePickerDialog.getDatePicker().getDayOfMonth()).toReturn(day);

        DateWidget dateWidget = new DateWidget(RuntimeEnvironment.application, formEntryPromptStub);
        Whitebox.setInternalState(dateWidget, "mDatePickerDialog", datePickerDialog);

        return dateWidget;
    }

    private DateTimeWidget prepareDateTimeWidget(int year, int month, int day, int hour, int minute) {
        QuestionDef questionDefStub = mock(QuestionDef.class);
        IFormElement iFormElementStub = mock(IFormElement.class);
        FormEntryPrompt formEntryPromptStub = mock(FormEntryPrompt.class);

        stub(iFormElementStub.getAdditionalAttribute(anyString(), anyString())).toReturn(null);
        stub(formEntryPromptStub.getQuestion()).toReturn(questionDefStub);
        stub(formEntryPromptStub.getFormElement()).toReturn(iFormElementStub);
        stub(formEntryPromptStub.getQuestion().getAppearanceAttr()).toReturn("no-calendar");

        DateWidget dateWidget = mock(DateWidget.class);
        stub(dateWidget.getYear()).toReturn(year);
        stub(dateWidget.getMonth()).toReturn(month);
        stub(dateWidget.getDay()).toReturn(day);

        TimeWidget timeWidget = mock(TimeWidget.class);
        stub(timeWidget.getHour()).toReturn(hour);
        stub(timeWidget.getMinute()).toReturn(minute);

        DateTimeWidget dateTimeWidget = new DateTimeWidget(RuntimeEnvironment.application, formEntryPromptStub);
        Whitebox.setInternalState(dateTimeWidget, "mDateWidget", dateWidget);
        Whitebox.setInternalState(dateTimeWidget, "mTimeWidget", timeWidget);

        return dateTimeWidget;
    }
}
