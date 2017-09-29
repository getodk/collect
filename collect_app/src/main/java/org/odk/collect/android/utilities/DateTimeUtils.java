package org.odk.collect.android.utilities;

import android.os.Build;

import org.odk.collect.android.widgets.AbstractDateWidget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static String getDateTimeBasedOnUserLocale(Date date, AbstractDateWidget.CalendarMode calendarMode, boolean containsTime) {
        String appearance = null;
        if (calendarMode.equals(AbstractDateWidget.CalendarMode.MONTH_YEAR)) {
            appearance = "month-year";
        } else if (calendarMode.equals(AbstractDateWidget.CalendarMode.YEAR)) {
            appearance = "year";
        }
        return getDateTimeBasedOnUserLocale(date, appearance, containsTime);
    }

    public static String getDateTimeBasedOnUserLocale(Date date, String appearance, boolean containsTime) {
        DateFormat dateFormatter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), getDateTimePattern(containsTime, appearance));
            dateFormatter = new SimpleDateFormat(format, Locale.getDefault());
        } else {
            dateFormatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
        }
        return dateFormatter.format(date);
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
}
