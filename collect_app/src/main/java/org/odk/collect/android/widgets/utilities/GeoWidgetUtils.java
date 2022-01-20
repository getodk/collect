package org.odk.collect.android.widgets.utilities;

import static org.odk.collect.shared.strings.StringUtils.removeEnd;

import android.content.Context;
import android.location.Location;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.analytics.AnalyticsUtils;
import org.odk.collect.android.utilities.FormEntryPromptUtils;

import java.text.DecimalFormat;

import timber.log.Timber;

public final class GeoWidgetUtils {

    private GeoWidgetUtils() {

    }

    public static String getGeoPointAnswerToDisplay(Context context, String answer) {
        try {
            if (answer != null && !answer.isEmpty()) {
                String[] parts = answer.split(" ");
                return context.getString(
                        R.string.gps_result,
                        convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[0]), "lat"),
                        convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[1]), "lon"),
                        truncateDouble(parts[2]),
                        truncateDouble(parts[3])
                );
            }
        } catch (NumberFormatException e) {
            return "";
        }
        return "";
    }

    public static String getGeoPolyAnswerToDisplay(String answer) {
        if (answer != null && !answer.isEmpty()) {
            answer = removeEnd(answer.replaceAll(";\\s", ";"), ";");
        }
        return answer;
    }

    public static double[] getLocationParamsFromStringAnswer(String answer) {
        double[] gp = new double[4];

        if (answer != null && !answer.isEmpty()) {
            String[] sa = answer.split(" ");

            try {
                gp[0] = Double.parseDouble(sa[0]);
                gp[1] = Double.parseDouble(sa[1]);
                gp[2] = Double.parseDouble(sa[2]);
                gp[3] = Double.parseDouble(sa[3]);
            } catch (Exception | Error e) {
                Timber.w(e);
            }
        }

        return gp;
    }

    static String convertCoordinatesIntoDegreeFormat(Context context, double coordinate, String type) {
        String coordinateDegrees = Location.convert(Math.abs(coordinate), Location.FORMAT_SECONDS);
        String[] coordinateSplit = coordinateDegrees.split(":");

        String degrees = floor(coordinateSplit[0]) + "°";
        String mins = floor(coordinateSplit[1]) + "'";
        String secs = floor(coordinateSplit[2]) + '"';

        return String.format(getCardinalDirection(context, coordinate, type), degrees, mins, secs);
    }

    static String floor(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.contains(".")
                ? value.substring(0, value.indexOf('.'))
                : value;
    }

    static String truncateDouble(String s) {
        DecimalFormat df = new DecimalFormat("#.##");
        try {
            return df.format(Double.valueOf(s));
        } catch (Exception | Error e) {
            Timber.w(e);
        }
        return "";
    }

    public static void logAllowMockAccuracy(FormEntryPrompt prompt) {
        String attributeValue = FormEntryPromptUtils.getBindAttribute(prompt, "allow-mock-accuracy");
        boolean allowMockAccuracy = Boolean.parseBoolean(attributeValue);

        if (allowMockAccuracy) {
            AnalyticsUtils.logFormEvent(AnalyticsEvents.ALLOW_MOCK_ACCURACY);
        }
    }

    private static String getCardinalDirection(Context context, double coordinate, String type) {
        return type.equalsIgnoreCase("lon")
                ? coordinate < 0 ? context.getString(R.string.west) : context.getString(R.string.east)
                : coordinate < 0 ? context.getString(R.string.south) : context.getString(R.string.north);
    }
}
