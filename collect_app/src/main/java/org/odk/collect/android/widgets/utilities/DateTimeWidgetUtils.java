package org.odk.collect.android.widgets.utilities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import org.javarosa.core.model.FormIndex;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.EthiopicChronology;
import org.joda.time.chrono.IslamicChronology;
import org.joda.time.chrono.PersianChronologyKhayyamBorkowski;
import org.odk.collect.android.R;
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
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.MyanmarDateUtils;
import org.odk.collect.android.utilities.ThemeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import bikramsambat.BikramSambatDate;
import bikramsambat.BsCalendar;
import bikramsambat.BsException;
import bikramsambat.BsGregorianDate;
import mmcalendar.MyanmarDate;
import mmcalendar.MyanmarDateConverter;
import timber.log.Timber;

public class DateTimeWidgetUtils {
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String DIALOG_THEME = "dialog_theme";
    public static final String DATE_PICKER_DETAILS = "date_picker_details";

    public static void setWidgetWaitingForData(FormIndex formIndex) {
        FormController formController = Collect.getInstance().getFormController();
        if (formController != null) {
            formController.setIndexWaitingForData(formIndex);
        }
    }

    public static DatePickerDetails getDatePickerDetails(String appearance) {
        DatePickerDetails.DatePickerType datePickerType = DatePickerDetails.DatePickerType.GREGORIAN;
        DatePickerDetails.DatePickerMode datePickerMode = DatePickerDetails.DatePickerMode.CALENDAR;
        if (appearance != null) {
            appearance = appearance.toLowerCase(Locale.US);
            if (appearance.contains(Appearances.ETHIOPIAN)) {
                datePickerType = DatePickerDetails.DatePickerType.ETHIOPIAN;
                datePickerMode = DatePickerDetails.DatePickerMode.SPINNERS;
            } else if (appearance.contains(Appearances.COPTIC)) {
                datePickerType = DatePickerDetails.DatePickerType.COPTIC;
                datePickerMode = DatePickerDetails.DatePickerMode.SPINNERS;
            } else if (appearance.contains(Appearances.ISLAMIC)) {
                datePickerType = DatePickerDetails.DatePickerType.ISLAMIC;
                datePickerMode = DatePickerDetails.DatePickerMode.SPINNERS;
            } else if (appearance.contains(Appearances.BIKRAM_SAMBAT)) {
                datePickerType = DatePickerDetails.DatePickerType.BIKRAM_SAMBAT;
                datePickerMode = DatePickerDetails.DatePickerMode.SPINNERS;
            } else if (appearance.contains(Appearances.MYANMAR)) {
                datePickerType = DatePickerDetails.DatePickerType.MYANMAR;
                datePickerMode = DatePickerDetails.DatePickerMode.SPINNERS;
            } else if (appearance.contains(Appearances.PERSIAN)) {
                datePickerType = DatePickerDetails.DatePickerType.PERSIAN;
                datePickerMode = DatePickerDetails.DatePickerMode.SPINNERS;
            } else if (appearance.contains(Appearances.NO_CALENDAR)) {
                datePickerMode = DatePickerDetails.DatePickerMode.SPINNERS;
            }

            if (appearance.contains(Appearances.MONTH_YEAR)) {
                datePickerMode = DatePickerDetails.DatePickerMode.MONTH_YEAR;
            } else if (appearance.contains(Appearances.YEAR)) {
                datePickerMode = DatePickerDetails.DatePickerMode.YEAR;
            }
        }

        return new DatePickerDetails(datePickerType, datePickerMode);
    }

    public static String getDateTimeLabel(Date date, DatePickerDetails datePickerDetails, boolean containsTime, Context context) {
        String gregorianDateText = getGregorianDateTimeLabel(date, datePickerDetails, containsTime, Locale.getDefault());

        DateTime customDate;
        String[] monthArray;

        switch (datePickerDetails.getDatePickerType()) {
            case GREGORIAN:
                return gregorianDateText;
            case ETHIOPIAN:
                customDate = new DateTime(date).withChronology(EthiopicChronology.getInstance());
                monthArray = context.getResources().getStringArray(R.array.ethiopian_months);
                break;
            case COPTIC:
                customDate = new DateTime(date).withChronology(CopticChronology.getInstance());
                monthArray = context.getResources().getStringArray(R.array.coptic_months);
                break;
            case ISLAMIC:
                customDate = new DateTime(date).withChronology(IslamicChronology.getInstance());
                monthArray = context.getResources().getStringArray(R.array.islamic_months);
                break;
            case BIKRAM_SAMBAT:
                customDate = new DateTime(date);
                monthArray = BsCalendar.MONTH_NAMES.toArray(new String[BsCalendar.MONTH_NAMES.size()]);
                break;
            case MYANMAR:
                customDate = new DateTime(date);
                MyanmarDate myanmarDate = MyanmarDateConverter.convert(customDate.getYear(),
                        customDate.getMonthOfYear(), customDate.getDayOfMonth(), customDate.getHourOfDay(),
                        customDate.getMinuteOfHour(), customDate.getSecondOfMinute());
                monthArray = MyanmarDateUtils.getMyanmarMonthsArray(myanmarDate.getYearInt());
                break;
            case PERSIAN:
                customDate = new DateTime(date).withChronology(PersianChronologyKhayyamBorkowski.getInstance());
                monthArray = context.getResources().getStringArray(R.array.persian_months);
                break;
            default:
                Timber.w("Not supported date type.");
                return null;
        }

        String customDateText = "";

        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        switch (datePickerDetails.getDatePickerType()) {
            case BIKRAM_SAMBAT:
                BikramSambatDate bikramSambatDate;
                try {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    bikramSambatDate = BsCalendar.getInstance().toBik(new BsGregorianDate(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH)));
                    String day = datePickerDetails.isSpinnerMode() ? bikramSambatDate.day + " " : "";
                    String month = datePickerDetails.isSpinnerMode() || datePickerDetails.isMonthYearMode() ? monthArray[bikramSambatDate.month - 1] + " " : "";

                    if (containsTime) {
                        customDateText = day + month + bikramSambatDate.year + ", " + df.format(customDate.toDate());
                    } else {
                        customDateText = day + month + bikramSambatDate.year;
                    }
                } catch (BsException e) {
                    Timber.e(e);
                }
                break;
            case MYANMAR: {
                MyanmarDate myanmarDate = MyanmarDateConverter.convert(customDate.getYear(),
                        customDate.getMonthOfYear(), customDate.getDayOfMonth(), customDate.getHourOfDay(),
                        customDate.getMinuteOfHour(), customDate.getSecondOfMinute());

                String day = datePickerDetails.isSpinnerMode() ? myanmarDate.getMonthDay() + " " : "";
                String month = datePickerDetails.isSpinnerMode() || datePickerDetails.isMonthYearMode() ? monthArray[MyanmarDateUtils.getMonthId(myanmarDate)] + " " : "";

                if (containsTime) {
                    customDateText = day + month + myanmarDate.getYearInt() + ", " + df.format(customDate.toDate());
                } else {
                    customDateText = day + month + myanmarDate.getYearInt();
                }
                break;
            }
            default:
                String day = datePickerDetails.isSpinnerMode() ? customDate.getDayOfMonth() + " " : "";
                String month = datePickerDetails.isSpinnerMode() || datePickerDetails.isMonthYearMode() ? monthArray[customDate.getMonthOfYear() - 1] + " " : "";

                if (containsTime) {
                    customDateText = day + month + customDate.getYear() + ", " + df.format(customDate.toDate());
                } else {
                    customDateText = day + month + customDate.getYear();
                }
        }

        return String.format(context.getString(R.string.custom_date), customDateText, gregorianDateText);
    }

    public void showTimePickerDialog(Context context, LocalDateTime dateTime) {
        ThemeUtils themeUtils = new ThemeUtils(context);

        Bundle bundle = new Bundle();
        bundle.putInt(DIALOG_THEME, themeUtils.getSpinnerTimePickerDialogTheme());
        bundle.putSerializable(TIME, dateTime);

        DialogUtils.showIfNotShowing(CustomTimePickerDialog.class, bundle, ((FragmentActivity) context).getSupportFragmentManager());
    }

    public void showDatePickerDialog(Context context, DatePickerDetails datePickerDetails, LocalDateTime date) {
        ThemeUtils themeUtils = new ThemeUtils(context);

        Bundle bundle = new Bundle();
        bundle.putInt(DIALOG_THEME, getDatePickerTheme(themeUtils, datePickerDetails));
        bundle.putSerializable(DATE, date);
        bundle.putSerializable(DATE_PICKER_DETAILS, datePickerDetails);

        DialogUtils.showIfNotShowing(getClass(datePickerDetails.getDatePickerType()), bundle, ((FragmentActivity) context).getSupportFragmentManager());
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

    private static int getDatePickerTheme(ThemeUtils themeUtils, DatePickerDetails datePickerDetails) {
        int theme = 0;
        if (!isBrokenSamsungDevice()) {
            theme = themeUtils.getCalendarDatePickerDialogTheme();
        }
        if (!datePickerDetails.isCalendarMode() || isBrokenSamsungDevice()) {
            theme = themeUtils.getSpinnerDatePickerDialogTheme();
        }

        return theme;
    }

    // https://stackoverflow.com/questions/28618405/datepicker-crashes-on-my-device-when-clicked-with-personal-app
    private static boolean isBrokenSamsungDevice() {
        return Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    private static String getGregorianDateTimeLabel(Date date, DatePickerDetails datePickerDetails, boolean containsTime, Locale locale) {
        DateFormat dateFormatter;
        locale = locale == null ? Locale.getDefault() : locale;
        String format = android.text.format.DateFormat.getBestDateTimePattern(locale, getDateTimeSkeleton(containsTime, datePickerDetails));
        dateFormatter = new SimpleDateFormat(format, locale);
        return dateFormatter.format(date);
    }

    private static String getDateTimeSkeleton(boolean containsTime, DatePickerDetails datePickerDetails) {
        String dateSkeleton;
        if (containsTime) {
            dateSkeleton = "yyyyMMMdd HHmm";
        } else {
            dateSkeleton = "yyyyMMMdd";
        }
        if (datePickerDetails.isMonthYearMode()) {
            dateSkeleton = "yyyyMMM";
        } else if (datePickerDetails.isYearMode()) {
            dateSkeleton = "yyyy";
        }
        return dateSkeleton;
    }
}
