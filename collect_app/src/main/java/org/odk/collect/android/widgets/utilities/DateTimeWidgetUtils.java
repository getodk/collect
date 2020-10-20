package org.odk.collect.android.widgets.utilities;

import android.os.Build;
import android.os.Bundle;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.TimeData;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.BikramSambatDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CopticDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog;
import org.odk.collect.android.fragments.dialogs.EthiopianDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.IslamicDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.MyanmarDatePickerDialog;
import org.odk.collect.android.fragments.dialogs.PersianDatePickerDialog;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.ThemeUtils;

import static org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog.CURRENT_TIME;
import static org.odk.collect.android.fragments.dialogs.CustomTimePickerDialog.TIME_PICKER_THEME;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.CURRENT_DATE;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.DATE_PICKER_DETAILS;
import static org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.DATE_PICKER_THEME;

public class DateTimeWidgetUtils {

    private DateTimeWidgetUtils() {
    }

    public static TimeData getTimeData(int hourOfDay, int minuteOfHour) {
        // use picker time, convert to today's date, store as utc
        DateTime localDateTime = new DateTime()
                .withTime(hourOfDay, minuteOfHour, 0, 0);

        return new TimeData(localDateTime.toDate());
    }

    public static void showDatePickerDialog(FormEntryActivity activity, FormIndex formIndex, DatePickerDetails datePickerDetails,
                                            LocalDateTime date) {
        switch (datePickerDetails.getDatePickerType()) {
            case ETHIOPIAN:
                CustomDatePickerDialog dialog = EthiopianDatePickerDialog.newInstance(formIndex, date, datePickerDetails);
                dialog.show(activity.getSupportFragmentManager(), CustomDatePickerDialog.class.getName());
                break;
            case COPTIC:
                dialog = CopticDatePickerDialog.newInstance(formIndex, date, datePickerDetails);
                dialog.show(activity.getSupportFragmentManager(), CustomDatePickerDialog.class.getName());
                break;
            case ISLAMIC:
                dialog = IslamicDatePickerDialog.newInstance(formIndex, date, datePickerDetails);
                dialog.show(activity.getSupportFragmentManager(), CustomDatePickerDialog.class.getName());
                break;
            case BIKRAM_SAMBAT:
                dialog = BikramSambatDatePickerDialog.newInstance(formIndex, date, datePickerDetails);
                dialog.show(activity.getSupportFragmentManager(), CustomDatePickerDialog.class.getName());
                break;
            case MYANMAR:
                dialog = MyanmarDatePickerDialog.newInstance(formIndex, date, datePickerDetails);
                dialog.show(activity.getSupportFragmentManager(), CustomDatePickerDialog.class.getName());
                break;
            case PERSIAN:
                dialog = PersianDatePickerDialog.newInstance(formIndex, date, datePickerDetails);
                dialog.show(activity.getSupportFragmentManager(), CustomDatePickerDialog.class.getName());
                break;
            default:
                ThemeUtils themeUtils = new ThemeUtils(activity);

                Bundle bundle = new Bundle();
                bundle.putInt(DATE_PICKER_THEME, getTheme(themeUtils, datePickerDetails));
                bundle.putSerializable(CURRENT_DATE, date);
                bundle.putSerializable(DATE_PICKER_DETAILS, datePickerDetails);

                DialogUtils.showIfNotShowing(FixedDatePickerDialog.class, bundle, activity.getSupportFragmentManager());
        }
    }

    public static void createTimePickerDialog(FormEntryActivity activity, int hourOfDay, int minuteOfHour) {
        ThemeUtils themeUtils = new ThemeUtils(activity);
        Bundle bundle = new Bundle();
        bundle.putInt(TIME_PICKER_THEME, themeUtils.getHoloDialogTheme());
        bundle.putSerializable(CURRENT_TIME, new DateTime().withTime(hourOfDay, minuteOfHour, 0, 0));

        DialogUtils.showIfNotShowing(CustomTimePickerDialog.class, bundle, activity.getSupportFragmentManager());
    }

    private static int getTheme(ThemeUtils themeUtils, DatePickerDetails datePickerDetails) {
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
