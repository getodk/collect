package org.odk.collect.android.widgets.utilities;

import android.content.Context;
import android.location.Location;

import org.odk.collect.android.R;

import java.text.DecimalFormat;

import timber.log.Timber;

public class GeoWidgetUtils {

    private GeoWidgetUtils() {
    }

    public static String convertCoordinatesIntoDegreeFormat(Context context, double coordinate, String type) {
        String coordinateDegrees = Location.convert(Math.abs(coordinate), Location.FORMAT_SECONDS);
        String[] coordinateSplit = coordinateDegrees.split(":");

        String degrees = floor(coordinateSplit[0]) + "°";
        String mins = floor(coordinateSplit[1]) + "'";
        String secs = floor(coordinateSplit[2]) + '"';

        return String.format(getCardinalDirection(context, coordinate, type), degrees, mins, secs);
    }

    private static String getCardinalDirection(Context context, double coordinate, String type) {
        return type.equalsIgnoreCase("lon")
                ? coordinate < 0 ? context.getString(R.string.west) : context.getString(R.string.east)
                : coordinate < 0 ? context.getString(R.string.south) : context.getString(R.string.north);
    }

    static String floor(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.contains(".")
                ? value.substring(0, value.indexOf('.'))
                : value;
    }

    public static double[] getLocationParamsFromStringAnswer(String answer) {
        double[] gp = new double[4];

        if (answer != null && !answer.isEmpty()) {
            String[] sa = answer.split(" ");

            try {
                gp[0] = Double.valueOf(sa[0]);
                gp[1] = Double.valueOf(sa[1]);
                gp[2] = Double.valueOf(sa[2]);
                gp[3] = Double.valueOf(sa[3]);
            } catch (Exception | Error e) {
                Timber.w(e);
            }
        }

        return gp;
    }

    public static String truncateDouble(String s) {
        DecimalFormat df = new DecimalFormat("#.##");
        try {
            return df.format(Double.valueOf(s));
        } catch (Exception | Error e) {
            Timber.w(e);
        }
        return "";
    }
}