package org.odk.collect.android.widgets.utilities;

import android.os.Build;
import android.os.Bundle;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.TimeData;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.forms.Form;
import org.odk.collect.android.fragments.dialogs.BikramSambatDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CopticDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog;
import org.odk.collect.android.fragments.dialogs.EthiopianDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.IslamicDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.MyanmarDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.PersianDatePickerDialog;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.ThemeUtils;

public class DateTimeWidgetUtils {
    public static final String FORM_INDEX = "formIndex";
    public static final String DATE = "date";
    public static final String DATE_PICKER_DETAILS = "datePickerDetails";
    public static final String DATE_PICKER_THEME = "datePickerTheme";

    private DateTimeWidgetUtils() {
    }

    public static TimeData getTimeData(DateTime dateTime) {
        // use picker time, convert to today's date, store as utc
        DateTime localDateTime = new DateTime().withTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), 0, 0);
        return new TimeData(localDateTime.toDate());
    }

    public static LocalDateTime getCurrentDate() {
        return LocalDateTime
                .now()
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
    }

    public static void setWidgetWaitingForData(FormIndex index) {
        FormController formController = Collect.getInstance().getFormController();
        if (formController != null) {
            formController.setIndexWaitingForData(index);
        }
    }

    public static void showDatePickerDialog(FormEntryActivity activity, FormIndex formIndex, DatePickerDetails datePickerDetails,
                                            LocalDateTime date) {
        ThemeUtils themeUtils = new ThemeUtils(activity);

        Bundle bundle = new Bundle();
        bundle.putInt(DATE_PICKER_THEME, getDatePickerTheme(themeUtils, datePickerDetails));
        bundle.putSerializable(DATE, date);
        bundle.putSerializable(DATE_PICKER_DETAILS, datePickerDetails);
        bundle.putSerializable(FORM_INDEX, formIndex);

        DialogUtils.showIfNotShowing(getClass(datePickerDetails.getDatePickerType()), bundle, activity.getSupportFragmentManager());
    }

    private static Class getClass(DatePickerDetails.DatePickerType datePickerType) {
        switch (datePickerType) {
            case ETHIOPIAN:
                return EthiopianDatePickerDialog.class;
            case COPTIC:
                return CopticDatePickerDialog.class;
            case ISLAMIC:
                return IslamicDatePickerDialog.class;
            case BIKRAM_SAMBAT:
                return BikramSambatDatePickerDialog.class;
            case MYANMAR:
                return MyanmarDatePickerDialog.class;
            case PERSIAN:
                return PersianDatePickerDialog.class;
            default:
                return FixedDatePickerDialog.class;
        }
    }

    public static void showTimePickerDialog(FormEntryActivity activity, LocalDateTime dateTime) {
        ThemeUtils themeUtils = new ThemeUtils(activity);
        Bundle bundle = new Bundle();
        bundle.putInt(CustomTimePickerDialog.TIME_PICKER_THEME, themeUtils.getHoloDialogTheme());
        bundle.putSerializable(CustomTimePickerDialog.CURRENT_TIME, dateTime);

        DialogUtils.showIfNotShowing(CustomTimePickerDialog.class, bundle, activity.getSupportFragmentManager());
    }

    private static int getDatePickerTheme(ThemeUtils themeUtils, DatePickerDetails datePickerDetails) {
        int theme = 0;
        if (!isBrokenSamsungDevice()) {
            theme = themeUtils.getMaterialDialogTheme();
        }
        if (!datePickerDetails.isCalendarMode() || isBrokenSamsungDevice()) {
            theme = themeUtils.getHoloDialogTheme();
        }

        return theme;
    }

    // https://stackoverflow.com/questions/28618405/datepicker-crashes-on-my-device-when-clicked-with-personal-app
    private static boolean isBrokenSamsungDevice() {
        return Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
    }
}
