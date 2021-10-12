package org.odk.collect.geo;

import android.location.Location;

import org.odk.collect.geo.maps.MapPoint;
import org.odk.collect.shared.strings.StringUtils;

import java.util.List;
import java.util.Locale;

public final class GeoUtils {

    private GeoUtils() {

    }

    /**
     * Serializes a list of vertices into a string, in the format
     * appropriate for storing as the result of the form question.
     */
    public static String formatPointsResultString(List<MapPoint> points, boolean isShape) {
        if (isShape) {
            // Polygons are stored with a last point that duplicates the
            // first point.  Add this extra point if it's not already present.
            int count = points.size();
            if (count > 1 && !points.get(0).equals(points.get(count - 1))) {
                points.add(points.get(0));
            }
        }
        StringBuilder result = new StringBuilder();
        for (MapPoint point : points) {
            // TODO(ping): Remove excess precision when we're ready for the output to change.
            result.append(String.format(Locale.US, "%s %s %s %s;",
                    Double.toString(point.lat), Double.toString(point.lon),
                    Double.toString(point.alt), Float.toString((float) point.sd)));
        }

        return StringUtils.removeEnd(result.toString().trim(), ";");
    }

    public static String formatLocationResultString(Location location) {
        return String.format("%s %s %s %s", location.getLatitude(), location.getLongitude(),
                location.getAltitude(), location.getAccuracy());
    }

    /**
     * Corrects location provider names so "gps" displays as "GPS" in user-facing messaging.
     */
    public static String capitalizeGps(String locationProvider) {
        return "gps".equals(locationProvider) ? "GPS" : locationProvider;
    }
}
