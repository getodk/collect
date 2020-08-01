package org.odk.collect.android.widgets.utilities;

import org.javarosa.core.model.FormIndex;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.BikramSambatDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CopticDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog;
import org.odk.collect.android.fragments.dialogs.EthiopianDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.IslamicDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.MyanmarDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.PersianDatePickerDialog;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

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

@RunWith(RobolectricTestRunner.class)
public class DateTimeWidgetUtilsTest {

    private DateTimeWidgetUtils dateTimeWidgetUtils;

    private FormEntryActivity activity;
    private FormIndex formIndex;
    private DatePickerDetails datePickerDetails;
    private LocalDateTime date;

    @Before
    public void setUp() {
        dateTimeWidgetUtils = new DateTimeWidgetUtils();

        activity = RobolectricHelpers.createThemedActivity(FormEntryActivity.class, 0);
        datePickerDetails = mock(DatePickerDetails.class);
        formIndex = mock(FormIndex.class);

        when(datePickerDetails.getDatePickerType()).thenReturn(GREGORIAN);
        date  = new LocalDateTime().withYear(2010).withMonthOfYear(5).withDayOfMonth(12);
    }

    @Test
    public void displayTimePickerDialog_showsCustomTimePickerDialog() {
        dateTimeWidgetUtils.displayTimePickerDialog(activity, new LocalDateTime().withHourOfDay(12).withMinuteOfHour(10));
        CustomTimePickerDialog dialog = (CustomTimePickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(CustomTimePickerDialog.class.getName());

        assertNotNull(dialog);
    }

    @Test
    public void displayDatePickerDialog_showsFixedDatePickerDialog_whenDatePickerTypeIsGregorian() {
        dateTimeWidgetUtils.displayDatePickerDialog(activity, formIndex, datePickerDetails, date);
        FixedDatePickerDialog dialog = (FixedDatePickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(FixedDatePickerDialog.class.getName());

        assertNotNull(dialog);
    }

    @Test
    public void displayDatePickerDialog_showsEthiopianDatePickerDialog_whenDatePickerTypeIsEthiopian() {
        when(datePickerDetails.getDatePickerType()).thenReturn(ETHIOPIAN);

        dateTimeWidgetUtils.displayDatePickerDialog(activity, formIndex, datePickerDetails, date);
        EthiopianDatePickerDialog dialog = (EthiopianDatePickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(EthiopianDatePickerDialog.class.getName());

        assertNotNull(dialog);
    }

    @Test
    public void displayDatePickerDialog_showsCopticDatePickerDialog_whenDatePickerTypeIsCoptic() {
        when(datePickerDetails.getDatePickerType()).thenReturn(COPTIC);

        dateTimeWidgetUtils.displayDatePickerDialog(activity, formIndex, datePickerDetails, date);
        CopticDatePickerDialog dialog = (CopticDatePickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(CopticDatePickerDialog.class.getName());

        assertNotNull(dialog);
    }

    @Test
    public void displayDatePickerDialog_showsIslamicDatePickerDialog_whenDatePickerTypeIsIslamic() {
        when(datePickerDetails.getDatePickerType()).thenReturn(ISLAMIC);

        dateTimeWidgetUtils.displayDatePickerDialog(activity, formIndex, datePickerDetails, date);
        IslamicDatePickerDialog dialog = (IslamicDatePickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(IslamicDatePickerDialog.class.getName());

        assertNotNull(dialog);
    }

    @Test
    public void displayDatePickerDialog_showsBikramSambatDatePickerDialog_whenDatePickerTypeIsBikramSambat() {
        when(datePickerDetails.getDatePickerType()).thenReturn(BIKRAM_SAMBAT);

        dateTimeWidgetUtils.displayDatePickerDialog(activity, formIndex, datePickerDetails, date);
        BikramSambatDatePickerDialog dialog = (BikramSambatDatePickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(BikramSambatDatePickerDialog.class.getName());

        assertNotNull(dialog);
    }

    @Test
    public void displayDatePickerDialog_showsMyanmarDatePickerDialog_whenDatePickerTypeIsMyanmar() {
        when(datePickerDetails.getDatePickerType()).thenReturn(MYANMAR);

        dateTimeWidgetUtils.displayDatePickerDialog(activity, formIndex, datePickerDetails, date);
        MyanmarDatePickerDialog dialog = (MyanmarDatePickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(MyanmarDatePickerDialog.class.getName());

        assertNotNull(dialog);
    }

    @Test
    public void displayDatePickerDialog_showsPersianDatePickerDialog_whenDatePickerTypeIsPersian() {
        when(datePickerDetails.getDatePickerType()).thenReturn(PERSIAN);

        dateTimeWidgetUtils.displayDatePickerDialog(activity, formIndex, datePickerDetails, date);
        PersianDatePickerDialog dialog = (PersianDatePickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(PersianDatePickerDialog.class.getName());

        assertNotNull(dialog);
    }
}
