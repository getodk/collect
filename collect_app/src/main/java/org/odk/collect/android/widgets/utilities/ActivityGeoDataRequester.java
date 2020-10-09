package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.interfaces.GeoDataRequester;

import java.text.DecimalFormat;

import timber.log.Timber;

public class ActivityGeoDataRequester implements GeoDataRequester {
    public static final String LOCATION = "gp";
    public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
    public static final String READ_ONLY = "readOnly";
    public static final String DRAGGABLE_ONLY = "draggable";
    public static final double DEFAULT_LOCATION_ACCURACY = 5.0;

    private final PermissionUtils permissionUtils;

    public ActivityGeoDataRequester() {
        permissionUtils = new PermissionUtils();
    }

    public ActivityGeoDataRequester(PermissionUtils permissionUtils) {
        this.permissionUtils = permissionUtils;
    }

    @Override
    public void requestGeoPoint(Context context, FormEntryPrompt prompt, WaitingForDataRegistry waitingForDataRegistry) {

        permissionUtils.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Bundle bundle = new Bundle();

                String answer = prompt.getAnswerText();
                if (answer != null && !answer.isEmpty()) {
                    bundle.putDoubleArray(LOCATION, getLocationParamsFromStringAnswer(answer));
                }
                bundle.putDouble(ACCURACY_THRESHOLD, getAccuracyThreshold(prompt.getQuestion()));
                bundle.putBoolean(READ_ONLY, prompt.isReadOnly());

                boolean isMapAppearance = true;
                if (WidgetAppearanceUtils.hasAppearance(prompt, WidgetAppearanceUtils.PLACEMENT_MAP)) {
                    bundle.putBoolean(DRAGGABLE_ONLY, true);
                } else if (WidgetAppearanceUtils.hasAppearance(prompt, WidgetAppearanceUtils.MAPS)) {
                    bundle.putBoolean(DRAGGABLE_ONLY, false);
                } else {
                    isMapAppearance = false;
                }

                Intent intent = new Intent(context, isMapAppearance ? GeoPointMapActivity.class : GeoPointActivity.class);
                intent.putExtras(bundle);
                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.LOCATION_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }

    @Override
    public void requestGeoShape(Context context, FormEntryPrompt prompt, WaitingForDataRegistry waitingForDataRegistry) {
        permissionUtils.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Intent intent = new Intent(context, GeoPolyActivity.class);
                intent.putExtra(GeoPolyActivity.ANSWER_KEY, prompt.getAnswerText());
                intent.putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOSHAPE);
                intent.putExtra(READ_ONLY, prompt.isReadOnly());

                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }

    @Override
    public void requestGeoTrace(Context context, FormEntryPrompt prompt, WaitingForDataRegistry waitingForDataRegistry) {
        permissionUtils.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());

                Intent intent = new Intent(context, GeoPolyActivity.class);
                intent.putExtra(GeoPolyActivity.ANSWER_KEY, prompt.getAnswerText());
                intent.putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOTRACE);
                intent.putExtra(READ_ONLY, prompt.isReadOnly());

                ((Activity) context).startActivityForResult(intent, ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE);
            }

            @Override
            public void denied() {
            }
        });
    }

    public static String getAnswerToDisplay(Context context, String answer) {
        try {
            if (answer != null && !answer.isEmpty()) {
                String[] parts = answer.split(" ");
                return context.getString(
                        R.string.gps_result,
                        ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[0]), "lat"),
                        ActivityGeoDataRequester.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[1]), "lon"),
                        ActivityGeoDataRequester.truncateDouble(parts[2]),
                        ActivityGeoDataRequester.truncateDouble(parts[3])
                );
            }
        } catch (NumberFormatException e) {
            return "";
        }
        return "";
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

    private static String getCardinalDirection(Context context, double coordinate, String type) {
        return type.equalsIgnoreCase("lon")
                ? coordinate < 0 ? context.getString(R.string.west) : context.getString(R.string.east)
                : coordinate < 0 ? context.getString(R.string.south) : context.getString(R.string.north);
    }

    private static double getAccuracyThreshold(QuestionDef questionDef) {
        // Determine the accuracy threshold to use.
        String acc = questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD);
        return acc != null && !acc.isEmpty() ? Double.parseDouble(acc) : DEFAULT_LOCATION_ACCURACY;
    }
}