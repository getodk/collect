package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.databinding.GeoWidgetAnswerBinding;
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.PermissionUtils;

import java.text.DecimalFormat;

import timber.log.Timber;

public class GeoWidgetUtils {
    public static final String LOCATION = "gp";
    public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
    public static final String READ_ONLY = "readOnly";
    public static final String DRAGGABLE_ONLY = "draggable";
    public static final double DEFAULT_LOCATION_ACCURACY = 5.0;

    private GeoWidgetUtils() {
    }

    public static double getAccuracyThreshold(QuestionDef questionDef) {
        // Determine the accuracy threshold to use.
        String acc = questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD);
        return acc != null && !acc.isEmpty() ? Double.parseDouble(acc) : DEFAULT_LOCATION_ACCURACY;
    }

    public static IAnswerData getAnswer(String stringAnswer) {
        if (stringAnswer == null || stringAnswer.isEmpty()) {
            return null;
        } else {
            return new GeoPointData(GeoWidgetUtils.getLocationParamsFromStringAnswer(stringAnswer));
        }
    }

    public static String updateAnswer(Context context, GeoWidgetAnswerBinding binding, Object answer) {
        String stringAnswer = (String) answer;
        binding.geoAnswerText.setText(getAnswerToDisplay(context, stringAnswer));
        if (binding.geoAnswerText.getText().toString().equals("")) {
            stringAnswer = "";
        }
        return stringAnswer;
    }

    public static String getAnswerToDisplay(Context context, String answer) {
        try {
            if (answer != null && !answer.isEmpty()) {
                String[] parts = answer.split(" ");
                return context.getString(
                        R.string.gps_result,
                        GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[0]), "lat"),
                        GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(context, Double.parseDouble(parts[1]), "lon"),
                        GeoWidgetUtils.truncateDouble(parts[2]),
                        GeoWidgetUtils.truncateDouble(parts[3])
                );
            }
        } catch (NumberFormatException e) {
            return "";
        }
        return "";
    }

    public static Bundle getGeoPointBundle(String stringAnswer, double accuracyThreshold, Boolean readOnly, Boolean draggable) {
        Bundle bundle = new Bundle();
        if (stringAnswer != null && !stringAnswer.isEmpty()) {
            bundle.putDoubleArray(LOCATION, GeoWidgetUtils.getLocationParamsFromStringAnswer(stringAnswer));
        }
        bundle.putDouble(ACCURACY_THRESHOLD, accuracyThreshold);
        if (readOnly != null && draggable != null) {
            bundle.putBoolean(READ_ONLY, readOnly);
            bundle.putBoolean(DRAGGABLE_ONLY, draggable);
        }
        return bundle;
    }

    public static Bundle getGeoPolyActivityBundle(String stringAnswer, GeoPolyActivity.OutputMode outputMode) {
        Bundle bundle = new Bundle();
        bundle.putString(GeoPolyActivity.ANSWER_KEY, stringAnswer);
        bundle.putSerializable(GeoPolyActivity.OUTPUT_MODE_KEY, outputMode);
        return bundle;
    }

    public static void onButtonClick(Context context, FormEntryPrompt prompt, PermissionUtils permissionUtils, MapConfigurator mapConfigurator,
                                     WaitingForDataRegistry waitingForDataRegistry, Class activityClass, Bundle bundle, int requestCode) {
        permissionUtils.requestLocationPermissions((Activity) context, new PermissionListener() {
            @Override
            public void granted() {
                waitingForDataRegistry.waitForData(prompt.getIndex());
                GeoWidgetUtils.startGeoActivity(context, mapConfigurator, activityClass, bundle, requestCode);
            }

            @Override
            public void denied() {
            }
        });
    }

    private static void startGeoActivity(Context context, MapConfigurator mapConfigurator, Class activityClass, Bundle bundle, int requestCode) {
        if (mapConfigurator == null || mapConfigurator.isAvailable(context)) {
            Intent intent = new Intent(context, activityClass);
            intent.putExtras(bundle);
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            mapConfigurator.showUnavailableMessage(context);
        }
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