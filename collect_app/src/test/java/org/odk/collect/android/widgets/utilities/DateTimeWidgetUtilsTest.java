package org.odk.collect.android.widgets.utilities;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.BikramSambatDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CopticDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.EthiopianDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.IslamicDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.MyanmarDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.PersianDatePickerDialog;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.DATE_PICKER_DIALOG;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.BIKRAM_SAMBAT;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.COPTIC;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.ETHIOPIAN;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.GREGORIAN;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.ISLAMIC;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.MYANMAR;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.PERSIAN;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;

@RunWith(RobolectricTestRunner.class)
public class DateTimeWidgetUtilsTest {

    private FormEntryActivity activity;
    private FormEntryPrompt prompt;
    private DatePickerDetails datePickerDetails;
    private LocalDateTime date;

    @Before
    public void setUp() {
        activity = RobolectricHelpers.createThemedActivity(FormEntryActivity.class, 0);

        QuestionDef questionDef = mock(QuestionDef.class);
        datePickerDetails = mock(DatePickerDetails.class);
        prompt = promptWithQuestionDefAndAnswer(questionDef, null);

        when(datePickerDetails.getDatePickerType()).thenReturn(GREGORIAN);
        date  = new LocalDateTime().withYear(2010).withMonthOfYear(5).withDayOfMonth(12);
    }

    @Test
    public void showDatePickerDialog_showsFixedDatePickerDialog_withLightThemeForSpinnerMode() {
        when(datePickerDetails.isCalendarMode()).thenReturn(false);

        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        FixedDatePickerDialog dialog = (FixedDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(FixedDatePickerDialog.class.getName());

        assertNotNull(dialog);
        assertEquals(dialog.getTheme(), 0);
    }

    @Test
    public void showDatePickerDialog_showsFixedDatePickerDialog_withLightThemeForLightMode() {
        when(datePickerDetails.isCalendarMode()).thenReturn(true);

        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        FixedDatePickerDialog dialog = (FixedDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(FixedDatePickerDialog.class.getName());

        assertNotNull(dialog);
        assertEquals(dialog.getTheme(), 0);
    }

    @Test
    public void showDatePickerDialog_showsFixedDatePickerDialog_withDarkThemeForDarkMode() {
        when(datePickerDetails.isCalendarMode()).thenReturn(true);

        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        FixedDatePickerDialog dialog = (FixedDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(FixedDatePickerDialog.class.getName());

        assertNotNull(dialog);
        assertEquals(dialog.getTheme(), 1);
    }

    @Test
    public void showDatePickerDialog_showsEthiopianDatePickerDialog_whenDatePickerTypeIsEthiopian() {
        when(datePickerDetails.getDatePickerType()).thenReturn(ETHIOPIAN);

        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        EthiopianDatePickerDialog dialog = (EthiopianDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(DATE_PICKER_DIALOG);

        assertNotNull(dialog);
    }

    @Test
    public void showDatePickerDialog_showsCopticDatePickerDialog_whenDatePickerTypeIsCoptic() {
        when(datePickerDetails.getDatePickerType()).thenReturn(COPTIC);

        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        CopticDatePickerDialog dialog = (CopticDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(DATE_PICKER_DIALOG);

        assertNotNull(dialog);
    }

    @Test
    public void showDatePickerDialog_showsIslamicDatePickerDialog_whenDatePickerTypeIsIslamic() {
        when(datePickerDetails.getDatePickerType()).thenReturn(ISLAMIC);

        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        IslamicDatePickerDialog dialog = (IslamicDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(DATE_PICKER_DIALOG);

        assertNotNull(dialog);
    }

    @Test
    public void showDatePickerDialog_showsBikramSambatDatePickerDialog_whenDatePickerTypeIsBikramSambat() {
        when(datePickerDetails.getDatePickerType()).thenReturn(BIKRAM_SAMBAT);

        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        BikramSambatDatePickerDialog dialog = (BikramSambatDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(DATE_PICKER_DIALOG);

        assertNotNull(dialog);
    }

    @Test
    public void showDatePickerDialog_showsMyanmarDatePickerDialog_whenDatePickerTypeIsMyanmar() {
        when(datePickerDetails.getDatePickerType()).thenReturn(MYANMAR);

        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        MyanmarDatePickerDialog dialog = (MyanmarDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(DATE_PICKER_DIALOG);

        assertNotNull(dialog);
    }

    @Test
    public void showDatePickerDialog_showsPersianDatePickerDialog_whenDatePickerTypeIsPersian() {
        when(datePickerDetails.getDatePickerType()).thenReturn(PERSIAN);

        DateTimeWidgetUtils.showDatePickerDialog(activity, prompt, datePickerDetails, date);
        PersianDatePickerDialog dialog = (PersianDatePickerDialog) activity.getSupportFragmentManager().findFragmentByTag(DATE_PICKER_DIALOG);

        assertNotNull(dialog);
    }
}
