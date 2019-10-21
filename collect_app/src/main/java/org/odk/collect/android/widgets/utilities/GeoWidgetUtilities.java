package org.odk.collect.android.widgets.utilities;

import android.content.Context;

import org.odk.collect.android.R;

public class GeoWidgetUtilities {

    private GeoWidgetUtilities() {
    }

    public static String formatGps(Context context, double coordinates, String type) {
        String location = Double.toString(coordinates);
        String degreeSign = "°";
        String degree = location.substring(0, location.indexOf('.'))
                + degreeSign;
        location = "0." + location.substring(location.indexOf('.') + 1);
        double temp = Double.valueOf(location) * 60;
        location = Double.toString(temp);
        String mins = location.substring(0, location.indexOf('.')) + "'";

        location = "0." + location.substring(location.indexOf('.') + 1);
        temp = Double.valueOf(location) * 60;
        location = Double.toString(temp);
        String secs = location.substring(0, location.indexOf('.')) + '"';
        if (type.equalsIgnoreCase("lon")) {
            if (degree.startsWith("-")) {
                degree = String.format(context
                        .getString(R.string.west), degree.replace("-", ""), mins, secs);
            } else {
                degree = String.format(context
                        .getString(R.string.east), degree.replace("-", ""), mins, secs);
            }
        } else {
            if (degree.startsWith("-")) {
                degree = String.format(context
                        .getString(R.string.south), degree.replace("-", ""), mins, secs);
            } else {
                degree = String.format(context
                        .getString(R.string.north), degree.replace("-", ""), mins, secs);
            }
        }
        return degree;
    }
}
