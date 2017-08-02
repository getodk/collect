package org.odk.collect.android.utilities;

import android.os.Build;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static String getDateTimeBasedOnUserLocale(Date date, String appearance, boolean containsTime) {
        final DateFormat dateFormatter;
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
        if ("year".equals(appearance)) {
            datePattern = "yyyy";
        } else if ("month-year".equals(appearance)) {
            datePattern = "yyyyMMM";
        }
        return datePattern;
    }

    public static String getTimeBasedOnUserLocale(Date date) {
        final DateFormat dateFormatter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String format = android.text.format.DateFormat.getBestDateTimePattern(
                    Locale.getDefault(), "HHmm");
            dateFormatter = new SimpleDateFormat(format, Locale.getDefault());
        } else {
            dateFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.getDefault());
        }
        return dateFormatter.format(date);
    }

    /**
     * Adjusts Dates to have correct values in the current timezone.
     * @param date the Date to be adjusted
     * @return the adjusted date, as a DateTime object
     */
    public static DateTime correctForTimezoneOffsetDifference(Date date) {
        // We can tolerate the getTimezoneOffset deprecation until rewriting with Java 8 Date/Time
        int timezoneOffsetDiff = date.getTimezoneOffset() - (new Date()).getTimezoneOffset();
        return new DateTime(date.getTime()).plusMinutes(timezoneOffsetDiff);
    }
}
