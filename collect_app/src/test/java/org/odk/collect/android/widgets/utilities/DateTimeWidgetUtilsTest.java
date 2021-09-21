package org.odk.collect.android.widgets.utilities;

import androidx.fragment.app.DialogFragment;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.fragments.dialogs.BikramSambatDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CopticDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog;
import org.odk.collect.android.fragments.dialogs.EthiopianDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.IslamicDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.MyanmarDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.PersianDatePickerDialog;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.testshared.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContextActivity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.BIKRAM_SAMBAT;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.COPTIC;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.ETHIOPIAN;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.GREGORIAN;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.ISLAMIC;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.MYANMAR;
import static org.odk.collect.android.logic.DatePickerDetails.DatePickerType.PERSIAN;

import android.app.DatePickerDialog;

@RunWith(AndroidJUnit4.class)
public class DateTimeWidgetUtilsTest {
    private DateTimeWidgetUtils dateTimeWidgetUtils;

    private TestScreenContextActivity activity;
    private DatePickerDetails datePickerDetails;
    private LocalDateTime date;

    private DatePickerDetails gregorian;
    private DatePickerDetails gregorianSpinners;
    private DatePickerDetails gregorianMonthYear;
    private DatePickerDetails gregorianYear;

    private DatePickerDetails ethiopian;
    private DatePickerDetails ethiopianMonthYear;
    private DatePickerDetails ethiopianYear;

    private DatePickerDetails coptic;
    private DatePickerDetails copticMonthYear;
    private DatePickerDetails copticYear;

    private DatePickerDetails islamic;
    private DatePickerDetails islamicMonthYear;
    private DatePickerDetails islamicYear;

    private DatePickerDetails bikramSambat;
    private DatePickerDetails bikramSambatMonthYear;
    private DatePickerDetails bikramSambatYear;

    private DatePickerDetails myanmar;
    private DatePickerDetails myanmarMonthYear;
    private DatePickerDetails myanmarYear;

    private DatePickerDetails persian;
    private DatePickerDetails persianMonthYear;
    private DatePickerDetails persianYear;

    @Before
    public void setUp() {
        dateTimeWidgetUtils = new DateTimeWidgetUtils();

        activity = RobolectricHelpers.createThemedActivity(TestScreenContextActivity.class, R.style.Theme_MaterialComponents);
        datePickerDetails = mock(DatePickerDetails.class);

        when(datePickerDetails.getDatePickerType()).thenReturn(GREGORIAN);
        date  = new LocalDateTime().withYear(2010).withMonthOfYear(5).withDayOfMonth(12);

        gregorian = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.CALENDAR);
        gregorianSpinners = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.SPINNERS);
        gregorianMonthYear = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.MONTH_YEAR);
        gregorianYear = new DatePickerDetails(DatePickerDetails.DatePickerType.GREGORIAN, DatePickerDetails.DatePickerMode.YEAR);

        ethiopian = new DatePickerDetails(DatePickerDetails.DatePickerType.ETHIOPIAN, DatePickerDetails.DatePickerMode.SPINNERS);
        ethiopianMonthYear = new DatePickerDetails(DatePickerDetails.DatePickerType.ETHIOPIAN, DatePickerDetails.DatePickerMode.MONTH_YEAR);
        ethiopianYear = new DatePickerDetails(DatePickerDetails.DatePickerType.ETHIOPIAN, DatePickerDetails.DatePickerMode.YEAR);

        coptic = new DatePickerDetails(DatePickerDetails.DatePickerType.COPTIC, DatePickerDetails.DatePickerMode.SPINNERS);
        copticMonthYear = new DatePickerDetails(DatePickerDetails.DatePickerType.COPTIC, DatePickerDetails.DatePickerMode.MONTH_YEAR);
        copticYear = new DatePickerDetails(DatePickerDetails.DatePickerType.COPTIC, DatePickerDetails.DatePickerMode.YEAR);

        islamic = new DatePickerDetails(DatePickerDetails.DatePickerType.ISLAMIC, DatePickerDetails.DatePickerMode.SPINNERS);
        islamicMonthYear = new DatePickerDetails(DatePickerDetails.DatePickerType.ISLAMIC, DatePickerDetails.DatePickerMode.MONTH_YEAR);
        islamicYear = new DatePickerDetails(DatePickerDetails.DatePickerType.ISLAMIC, DatePickerDetails.DatePickerMode.YEAR);

        bikramSambat = new DatePickerDetails(DatePickerDetails.DatePickerType.BIKRAM_SAMBAT, DatePickerDetails.DatePickerMode.SPINNERS);
        bikramSambatMonthYear = new DatePickerDetails(DatePickerDetails.DatePickerType.BIKRAM_SAMBAT, DatePickerDetails.DatePickerMode.MONTH_YEAR);
        bikramSambatYear = new DatePickerDetails(DatePickerDetails.DatePickerType.BIKRAM_SAMBAT, DatePickerDetails.DatePickerMode.YEAR);

        myanmar = new DatePickerDetails(DatePickerDetails.DatePickerType.MYANMAR, DatePickerDetails.DatePickerMode.SPINNERS);
        myanmarMonthYear = new DatePickerDetails(DatePickerDetails.DatePickerType.MYANMAR, DatePickerDetails.DatePickerMode.MONTH_YEAR);
        myanmarYear = new DatePickerDetails(DatePickerDetails.DatePickerType.MYANMAR, DatePickerDetails.DatePickerMode.YEAR);

        persian = new DatePickerDetails(DatePickerDetails.DatePickerType.PERSIAN, DatePickerDetails.DatePickerMode.SPINNERS);
        persianMonthYear = new DatePickerDetails(DatePickerDetails.DatePickerType.PERSIAN, DatePickerDetails.DatePickerMode.MONTH_YEAR);
        persianYear = new DatePickerDetails(DatePickerDetails.DatePickerType.PERSIAN, DatePickerDetails.DatePickerMode.YEAR);
    }

    @Test
    public void getDatePickerDetailsTest() {
        assertEquals(gregorian, DateTimeWidgetUtils.getDatePickerDetails(null));
        String appearance = "something";
        assertEquals(gregorian, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "no-calendar";
        assertEquals(gregorianSpinners, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "NO-CALENDAR";
        assertEquals(gregorianSpinners, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "month-year";
        assertEquals(gregorianMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "MONTH-year";
        assertEquals(gregorianMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "year";
        assertEquals(gregorianYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Year";
        assertEquals(gregorianYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));

        appearance = "ethiopian";
        assertEquals(ethiopian, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Ethiopian month-year";
        assertEquals(ethiopianMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "month-year ethiopian";
        assertEquals(ethiopianMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Ethiopian year";
        assertEquals(ethiopianYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "year ethiopian";
        assertEquals(ethiopianYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));

        appearance = "coptic";
        assertEquals(coptic, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Coptic month-year";
        assertEquals(copticMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "month-year coptic";
        assertEquals(copticMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Coptic year";
        assertEquals(copticYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "year coptic";
        assertEquals(copticYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));

        appearance = "islamic";
        assertEquals(islamic, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Islamic month-year";
        assertEquals(islamicMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "month-year islamic";
        assertEquals(islamicMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Islamic year";
        assertEquals(islamicYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "year islamic";
        assertEquals(islamicYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));

        appearance = "bikram-sambat";
        assertEquals(bikramSambat, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Bikram-sambat month-year";
        assertEquals(bikramSambatMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "month-year bikram-sambat";
        assertEquals(bikramSambatMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Bikram-sambat year";
        assertEquals(bikramSambatYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "year bikram-sambat";
        assertEquals(bikramSambatYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));

        appearance = "myanmar";
        assertEquals(myanmar, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Myanmar month-year";
        assertEquals(myanmarMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "month-year myanmar";
        assertEquals(myanmarMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Myanmar year";
        assertEquals(myanmarYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "year myanmar";
        assertEquals(myanmarYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));

        appearance = "persian";
        assertEquals(persian, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Persian month-year";
        assertEquals(persianMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "month-year persian";
        assertEquals(persianMonthYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "Persian year";
        assertEquals(persianYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
        appearance = "year persian";
        assertEquals(persianYear, DateTimeWidgetUtils.getDatePickerDetails(appearance));
    }

    @Test
    public void displayTimePickerDialog_showsCustomTimePickerDialog() {
        dateTimeWidgetUtils.showTimePickerDialog(activity, new LocalDateTime().withHourOfDay(12).withMinuteOfHour(10));
        CustomTimePickerDialog dialog = (CustomTimePickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(CustomTimePickerDialog.class.getName());

        assertNotNull(dialog);
    }

    @Test
    public void displayDatePickerDialog_showsFixedDatePickerDialog_whenDatePickerTypeIsGregorian() {
        assertDialogIsShowing(GREGORIAN, FixedDatePickerDialog.class);
    }

    @Test
    public void displayDatePickerDialog_showsEthiopianDatePickerDialog_whenDatePickerTypeIsEthiopian() {
        assertDialogIsShowing(ETHIOPIAN, EthiopianDatePickerDialog.class);
    }

    @Test
    public void displayDatePickerDialog_showsCopticDatePickerDialog_whenDatePickerTypeIsCoptic() {
        assertDialogIsShowing(COPTIC, CopticDatePickerDialog.class);
    }

    @Test
    public void displayDatePickerDialog_showsIslamicDatePickerDialog_whenDatePickerTypeIsIslamic() {
        assertDialogIsShowing(ISLAMIC, IslamicDatePickerDialog.class);
    }

    @Test
    public void displayDatePickerDialog_showsBikramSambatDatePickerDialog_whenDatePickerTypeIsBikramSambat() {
        assertDialogIsShowing(BIKRAM_SAMBAT, BikramSambatDatePickerDialog.class);
    }

    @Test
    public void displayDatePickerDialog_showsMyanmarDatePickerDialog_whenDatePickerTypeIsMyanmar() {
        assertDialogIsShowing(MYANMAR, MyanmarDatePickerDialog.class);
    }

    @Test
    public void displayDatePickerDialog_showsPersianDatePickerDialog_whenDatePickerTypeIsPersian() {
        assertDialogIsShowing(PERSIAN, PersianDatePickerDialog.class);
    }

    @Test
    public void displayDatePickerDialogWithYearMode_showsDatePickerWithDayAndMonthFixedToJanuaryFirst() {
        when(datePickerDetails.getDatePickerType()).thenReturn(GREGORIAN);

        dateTimeWidgetUtils.showDatePickerDialog(activity, gregorianYear, date);
        DialogFragment dialog = (DialogFragment) activity.getSupportFragmentManager()
                .findFragmentByTag(FixedDatePickerDialog.class.getName());

        assertThat(((DatePickerDialog) dialog.getDialog()).getDatePicker().getYear(), is(date.getYear()));
        assertThat(((DatePickerDialog) dialog.getDialog()).getDatePicker().getMonth(), is(0));
        assertThat(((DatePickerDialog) dialog.getDialog()).getDatePicker().getDayOfMonth(), is(1));
    }

    private void assertDialogIsShowing(DatePickerDetails.DatePickerType datePickerType, Class dialogClass) {
        when(datePickerDetails.getDatePickerType()).thenReturn(datePickerType);

        dateTimeWidgetUtils.showDatePickerDialog(activity, datePickerDetails, date);
        DialogFragment dialog = (DialogFragment) activity.getSupportFragmentManager()
                .findFragmentByTag(dialogClass.getName());

        assertNotNull(dialog);
        assertEquals(dialog.getClass(), dialogClass);
    }
}
