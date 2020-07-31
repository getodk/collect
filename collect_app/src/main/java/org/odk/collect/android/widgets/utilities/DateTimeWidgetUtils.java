package org.odk.collect.android.widgets.utilities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.TimeData;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.application.Collect;
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
import org.odk.collect.android.utilities.ScreenContext;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.widgets.interfaces.DateTimeWidgetListener;

public class DateTimeWidgetUtils implements DateTimeWidgetListener {
    public static final String FORM_INDEX = "formIndex";
    public static final String DATE = "date";
    public static final String DATE_PICKER_DETAILS = "datePickerDetails";
    public static final String DATE_PICKER_THEME = "datePickerTheme";

    public DateTimeWidgetUtils() {
    }

    @Override
    public void setWidgetWaitingForData(FormIndex formIndex) {
        setWaitingForData(formIndex);
    }

    @Override
    public boolean isWidgetWaitingForData(FormIndex formIndex) {
        return isWaitingForData(formIndex);
    }

    @Override
    public void displayDatePickerDialog(Context context, FormIndex formIndex, DatePickerDetails datePickerDetails, LocalDateTime selectedDate) {
        showDatePickerDialog(context, formIndex, datePickerDetails, selectedDate);
    }

    @Override
    public void displayTimePickerDialog(Context context, LocalDateTime selectedTime) {
        showTimePickerDialog(context, selectedTime);
    }

    public static LocalDateTime getSelectedDate(LocalDateTime selectedDate, LocalDateTime currentTime) {
        return new LocalDateTime()
                .withDate(selectedDate.getYear(), selectedDate.getMonthOfYear(), selectedDate.getDayOfMonth())
                .withTime(currentTime.getHourOfDay(), currentTime.getMinuteOfHour(), 0, 0);
    }

    public static LocalDateTime getSelectedTime(LocalDateTime selectedTime, LocalDateTime currentDate) {
        return new LocalDateTime()
                .withDate(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth())
                .withTime(selectedTime.getHourOfDay(), selectedTime.getMinuteOfHour(), 0, 0);
    }

    public static LocalDateTime getCurrentDateTime() {
        return new LocalDateTime()
                .withDate(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth())
                .withTime(DateTime.now().getHourOfDay(), DateTime.now().getMinuteOfHour(), 0, 0);
    }

    public static TimeData getTimeData(DateTime dateTime) {
        // use picker time, convert to today's date, store as utc
        DateTime localDateTime = new DateTime().withTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), 0, 0);
        return new TimeData(localDateTime.toDate());
    }

    private static void setWaitingForData(FormIndex formIndex) {
        FormController formController = Collect.getInstance().getFormController();
        if (formController != null) {
            formController.setIndexWaitingForData(formIndex);
        }
    }

    private static boolean isWaitingForData(FormIndex formIndex) {
        FormControllerWaitingForDataRegistry waitingForDataRegistry = new FormControllerWaitingForDataRegistry();
        if (waitingForDataRegistry.isWaitingForData(formIndex)) {
            waitingForDataRegistry.cancelWaitingForData();
            return true;
        }
        return false;
    }

    private static void showDatePickerDialog(Context context, FormIndex formIndex, DatePickerDetails datePickerDetails,
                                            LocalDateTime date) {
        ThemeUtils themeUtils = new ThemeUtils(context);

        Bundle bundle = new Bundle();
        bundle.putInt(DATE_PICKER_THEME, getDatePickerTheme(themeUtils, datePickerDetails));
        bundle.putSerializable(DATE, date);
        bundle.putSerializable(DATE_PICKER_DETAILS, datePickerDetails);
        bundle.putSerializable(FORM_INDEX, formIndex);

        DialogUtils.showIfNotShowing(getClass(datePickerDetails.getDatePickerType()), bundle, ((ScreenContext) context).getActivity().getSupportFragmentManager());
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

    private static void showTimePickerDialog(Context context, LocalDateTime dateTime) {
        ThemeUtils themeUtils = new ThemeUtils(context);
        Bundle bundle = new Bundle();
        bundle.putInt(CustomTimePickerDialog.TIME_PICKER_THEME, themeUtils.getHoloDialogTheme());
        bundle.putSerializable(CustomTimePickerDialog.CURRENT_TIME, dateTime);

        DialogUtils.showIfNotShowing(CustomTimePickerDialog.class, bundle, ((AppCompatActivity) context).getSupportFragmentManager());
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
