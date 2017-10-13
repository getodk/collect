package org.odk.collect.android.utilities;

import android.content.Context;
import android.os.Build;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.odk.collect.android.R;
import org.odk.collect.android.widgets.AbstractDateWidget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static String getDateTimeLabel(Date date, String appearance, boolean containsTime, Context context) {
        if (appearance != null && appearance.contains("ethiopian")) {
            return getEthiopianDateTimeLabel(date, appearance, containsTime, context);
        } else {
            return getGregorianDateTimeLabel(date, appearance, containsTime, null);
        }
    }

    private static String getGregorianDateTimeLabel(Date date, String appearance, boolean containsTime, Locale locale) {
        DateFormat dateFormatter;
        locale = locale == null ? Locale.getDefault() : locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String format = android.text.format.DateFormat.getBestDateTimePattern(locale, getDateTimePattern(containsTime, appearance));
            dateFormatter = new SimpleDateFormat(format, locale);
        } else {
            dateFormatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
        }
        return dateFormatter.format(date);
    }

    private static String getEthiopianDateTimeLabel(Date date, String appearance, boolean containsTime, Context context) {
        String gregorianDateText = getGregorianDateTimeLabel(date, appearance, containsTime, Locale.US);

        DateTime ethiopianDate = new DateTime(date).withChronology(EthiopicChronology.getInstance());
        boolean showDay = appearance == null || (!appearance.contains("month-year") && !appearance.contains("year"));
        boolean showMonth = showDay || appearance.contains("month-year");

        String day = showDay ? ethiopianDate.getDayOfMonth() + " " : "";
        String month = showMonth ? context.getResources().getStringArray(R.array.ethiopian_months)[ethiopianDate.getMonthOfYear() - 1] + " " : "";

        String ethiopianDateText = day + month + ethiopianDate.getYear();
        return String.format(context.getString(R.string.ethiopian_date), ethiopianDateText, gregorianDateText);
    }

    private static String getDateTimePattern(boolean containsTime, String appearance) {
        String datePattern;
        if (containsTime) {
            datePattern = "yyyyMMMdd HHmm";
        } else {
            datePattern = "yyyyMMMdd";
        }
        if (appearance != null) {
            if (appearance.contains("month-year")) {
                datePattern = "yyyyMMM";
            } else if (appearance.contains("year")) {
                datePattern = "yyyy";
            }
        }
        return datePattern;
    }

    public static LocalDateTime skipDaylightSavingGapIfExists(LocalDateTime date) {
        final DateTimeZone dtz = DateTimeZone.getDefault();
        while (dtz.isLocalDateTimeGap(date)) {
            date = date.plusMinutes(1);
        }
        return date;
    }

    public static String getAppearanceBasedOnCalendarMode(AbstractDateWidget.CalendarMode calendarMode) {
        String appearance = null;
        if (calendarMode.equals(AbstractDateWidget.CalendarMode.MONTH_YEAR)) {
            appearance = "month-year";
        } else if (calendarMode.equals(AbstractDateWidget.CalendarMode.YEAR)) {
            appearance = "year";
        }

        return appearance;
    }
}
