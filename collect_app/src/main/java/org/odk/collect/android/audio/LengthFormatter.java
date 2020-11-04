package org.odk.collect.android.audio;

import java.util.Locale;

import static java.lang.String.format;

public class LengthFormatter {

    public static final int ONE_HOUR = 3600000;
    public static final int ONE_MINUTE = 60000;
    public static final int ONE_SECOND = 1000;

    private LengthFormatter() {
        
    }

    public static String formatLength(long milliseconds) {
        long hours = milliseconds / ONE_HOUR;
        long minutes = (milliseconds % ONE_HOUR) / ONE_MINUTE;
        long seconds = (milliseconds % ONE_MINUTE) / ONE_SECOND;

        if (milliseconds < ONE_HOUR) {
            return format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        } else {
            return format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        }
    }
}
