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

import android.widget.DatePicker;
import android.widget.TimePicker;

import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.widgets.DateTimeWidget;
import org.odk.collect.android.widgets.DateWidget;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml",
        packageName = "org.odk.collect")
// https://github.com/opendatakit/collect/issues/356
// The purpose of this test is to confirm that the app doesn't crash anymore in that case
public class DaylightSavingTest {

    private static final String EST_TIME_ZONE = "America/Los_Angeles";
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
    // 12 Mar 2017 at 02:00:00 clocks were turned forward to 03:00:00
    public void testESTTimeZoneWithDateTimeWidget() {
        TimeZone.setDefault(TimeZone.getTimeZone(EST_TIME_ZONE));
        DateTimeWidget dateTimeWidget = prepareDateTimeWidget(2017, 2, 12, 2, 0);

        IAnswerData answerData = dateTimeWidget.getAnswer();
        assertNotNull(answerData);
        assertDate(answerData, 2017, 2, 12, 3, 0);
    }

    @Test
    // 12 Mar 2017 at 02:00:00 clocks were turned forward to 03:00:00
    public void testESTTimezoneWithDateWidget() {
        TimeZone.setDefault(TimeZone.getTimeZone(EST_TIME_ZONE));
        DateWidget dateWidget = prepareDateWidget(2017, 2, 12);

        IAnswerData answerData = dateWidget.getAnswer();
        assertNotNull(answerData);
        assertDate(answerData, 2017, 2, 12, 0, 0);
    }

    @Test
    // 26 Mar 2017 at 02:00:00 clocks were turned forward to 03:00:00
    public void testCETTimeZoneWithDateTimeWidget() {
        TimeZone.setDefault(TimeZone.getTimeZone(CET_TIME_ZONE));
        DateTimeWidget dateTimeWidget = prepareDateTimeWidget(2017, 2, 26, 2, 0);

        IAnswerData answerData = dateTimeWidget.getAnswer();
        assertNotNull(answerData);
        assertDate(answerData, 2017, 2, 26, 3, 0);
    }

    @Test
    // 26 Mar 2017 at 02:00:00 clocks were turned forward to 03:00:00
    public void testCETTimezoneWithDateWidget() {
        TimeZone.setDefault(TimeZone.getTimeZone(CET_TIME_ZONE));
        DateWidget dateWidget = prepareDateWidget(2017, 2, 26);

        IAnswerData answerData = dateWidget.getAnswer();
        assertNotNull(answerData);
        assertDate(answerData, 2017, 2, 26, 0, 0);
    }

    private void assertDate(IAnswerData answerData, int year, int month, int day, int hour, int minute) {
        Date date = (Date) answerData.getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        assertEquals(year, calendar.get(Calendar.YEAR));
        assertEquals(month, calendar.get(Calendar.MONTH));
        assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, calendar.get(Calendar.MINUTE));
    }

    private DateWidget prepareDateWidget(int year, int month, int day) {
        QuestionDef questionDefStub = mock(QuestionDef.class);
        IFormElement iFormElementStub = mock(IFormElement.class);
        FormEntryPrompt formEntryPromptStub = mock(FormEntryPrompt.class);

        stub(iFormElementStub.getAdditionalAttribute(anyString(), anyString())).toReturn(null);
        stub(formEntryPromptStub.getQuestion()).toReturn(questionDefStub);
        stub(formEntryPromptStub.getFormElement()).toReturn(iFormElementStub);
        stub(formEntryPromptStub.getQuestion().getAppearanceAttr()).toReturn("no-calendar");

        DatePicker datePicker = mock(DatePicker.class);
        stub(datePicker.getYear()).toReturn(year);
        stub(datePicker.getMonth()).toReturn(month);
        stub(datePicker.getDayOfMonth()).toReturn(day);

        DateWidget dateWidget = new DateWidget(RuntimeEnvironment.application, formEntryPromptStub);
        Whitebox.setInternalState(dateWidget, "mDatePicker", datePicker);

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

        DatePicker datePicker = mock(DatePicker.class);
        stub(datePicker.getYear()).toReturn(year);
        stub(datePicker.getMonth()).toReturn(month);
        stub(datePicker.getDayOfMonth()).toReturn(day);

        TimePicker timePicker = mock(TimePicker.class);
        stub(timePicker.getCurrentHour()).toReturn(hour);
        stub(timePicker.getCurrentMinute()).toReturn(minute);

        DateTimeWidget dateTimeWidget = new DateTimeWidget(RuntimeEnvironment.application, formEntryPromptStub);
        Whitebox.setInternalState(dateTimeWidget, "mDatePicker", datePicker);
        Whitebox.setInternalState(dateTimeWidget, "mTimePicker", timePicker);

        return dateTimeWidget;
    }
}
