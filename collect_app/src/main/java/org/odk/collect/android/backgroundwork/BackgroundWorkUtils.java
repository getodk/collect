package org.odk.collect.android.backgroundwork;

import android.content.Context;

import org.odk.collect.android.R;

public class BackgroundWorkUtils {

    private static final long FIFTEEN_MINUTES_PERIOD = 900000;
    private static final long ONE_HOUR_PERIOD = 3600000;
    private static final long SIX_HOURS_PERIOD = 21600000;
    private static final long ONE_DAY_PERIOD = 86400000;

    private BackgroundWorkUtils() {

    }

    public static long getPeriodInMilliseconds(String period, Context context) {
        if (period.equals(context.getString(R.string.every_one_hour_value))) {
            return ONE_HOUR_PERIOD;
        } else if (period.equals(context.getString(R.string.every_six_hours_value))) {
            return SIX_HOURS_PERIOD;
        } else if (period.equals(context.getString(R.string.every_24_hours_value))) {
            return ONE_DAY_PERIOD;
        } else if (period.equals(context.getString(R.string.every_fifteen_minutes_value))) {
            return FIFTEEN_MINUTES_PERIOD;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
