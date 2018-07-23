/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.activities.GeoPointOsmMapActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.PlayServicesUtil;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import java.text.DecimalFormat;
import java.util.Locale;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;
import static org.odk.collect.android.utilities.PermissionUtils.requestLocationPermissions;

/**
 * GeoPointWidget is the widget that allows the user to get GPS readings.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Jon Nordling (jonnordling@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class GeoPointWidget extends QuestionWidget implements BinaryWidget {
    public static final String LOCATION = "gp";
    public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
    public static final String READ_ONLY = "readOnly";
    public static final String DRAGGABLE_ONLY = "draggable";

    public static final double DEFAULT_LOCATION_ACCURACY = 5.0;
    private static final String GOOGLE_MAP_KEY = "google_maps";
    private final boolean readOnly;
    private final boolean useMapsV2;
    private final Button getLocationButton;
    private final Button viewButton;
    private final String mapSDK;
    private final TextView answerDisplay;
    private boolean useMaps;
    private double accuracyThreshold;
    private boolean draggable = true;

    private String stringAnswer;

    public GeoPointWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        // Determine the activity threshold to use
        String acc = prompt.getQuestion().getAdditionalAttribute(null, ACCURACY_THRESHOLD);
        if (acc != null && acc.length() != 0) {
            accuracyThreshold = Double.parseDouble(acc);
        } else {
            accuracyThreshold = DEFAULT_LOCATION_ACCURACY;
        }

        // Determine whether or not to use the plain, maps, or mapsV2 activity
        String appearance = prompt.getAppearanceHint();

        // use mapsV2 if it is available and was requested;
        useMapsV2 = useMapsV2(context);
        if (appearance != null && appearance.toLowerCase(Locale.US).contains("placement-map") && useMapsV2) {
            draggable = true;
            useMaps = true;
        } else if (appearance != null && appearance.toLowerCase(Locale.US).contains("maps") && useMapsV2) {
            draggable = false;
            useMaps = true;
        } else {
            useMaps = false;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mapSDK = sharedPreferences.getString(PreferenceKeys.KEY_MAP_SDK, GOOGLE_MAP_KEY);

        readOnly = prompt.isReadOnly();

        answerDisplay = getCenteredAnswerTextView();

        viewButton = getSimpleButton(getContext().getString(R.string.get_point), R.id.get_point);

        getLocationButton = getSimpleButton(R.id.get_location);
        getLocationButton.setEnabled(!prompt.isReadOnly());

        // finish complex layout
        // control what gets shown with setVisibility(View.GONE)
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(getLocationButton);
        answerLayout.addView(viewButton);
        answerLayout.addView(answerDisplay);
        addAnswerView(answerLayout);

        // Set vars Label/text for button enable view or collect...
        boolean dataAvailable = false;
        String s = prompt.getAnswerText();
        if (s != null && !s.equals("")) {
            dataAvailable = true;
            setBinaryData(s);
        }
        updateButtonLabelsAndVisibility(dataAvailable);

    }

    private void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        // BUT for mapsV2, we only show the getLocationButton, altering its text.
        // for maps, we show the view button.

        if (useMapsV2 && useMaps) {
            // show the GetLocation button
            getLocationButton.setVisibility(View.VISIBLE);
            // hide the view button
            viewButton.setVisibility(View.GONE);
            if (readOnly) {
                //READ_ONLY View
                getLocationButton.setText(
                        getContext().getString(R.string.geopoint_view_read_only));
            } else {
                if (stringAnswer != null && !stringAnswer.isEmpty()) {
                    getLocationButton.setText(
                            getContext().getString(R.string.view_change_location));
                } else {
                    getLocationButton.setText(getContext().getString(R.string.get_point));
                }
            }
        } else {
            // if it is read-only, hide the get-location button...
            if (readOnly) {
                getLocationButton.setVisibility(View.GONE);
            } else {
                getLocationButton.setVisibility(View.VISIBLE);
                getLocationButton.setText(getContext().getString(
                        dataAvailable ? R.string.change_location : R.string.get_point));
            }

            if (useMaps) {
                // show the view button
                viewButton.setVisibility(View.VISIBLE);
                viewButton.setEnabled(dataAvailable);
            } else {
                viewButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void clearAnswer() {
        stringAnswer = null;
        answerDisplay.setText(null);
        updateButtonLabelsAndVisibility(false);
    }

    @Override
    public IAnswerData getAnswer() {
        if (stringAnswer == null || stringAnswer.isEmpty()) {
            return null;
        } else {
            try {
                // segment lat and lon
                String[] sa = stringAnswer.split(" ");
                double[] gp = new double[4];
                gp[0] = Double.valueOf(sa[0]);
                gp[1] = Double.valueOf(sa[1]);
                gp[2] = Double.valueOf(sa[2]);
                gp[3] = Double.valueOf(sa[3]);

                return new GeoPointData(gp);

            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

    private String truncateDouble(String s) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(Double.valueOf(s));
    }

    private String formatGps(double coordinates, String type) {
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
                degree = String.format(getContext()
                        .getString(R.string.west), degree.replace("-", ""), mins, secs);
            } else {
                degree = String.format(getContext()
                        .getString(R.string.east), degree.replace("-", ""), mins, secs);
            }
        } else {
            if (degree.startsWith("-")) {
                degree = String.format(getContext()
                        .getString(R.string.south), degree.replace("-", ""), mins, secs);
            } else {
                degree = String.format(getContext()
                        .getString(R.string.north), degree.replace("-", ""), mins, secs);
            }
        }
        return degree;
    }

    @Override
    public void setBinaryData(Object answer) {
        String s = (String) answer;

        if (s != null && !s.isEmpty()) {
            stringAnswer = s;
            String[] sa = s.split(" ");
            answerDisplay.setText(String.format(getContext().getString(R.string.gps_result),
                    formatGps(Double.parseDouble(sa[0]), "lat"),
                    formatGps(Double.parseDouble(sa[1]), "lon"), truncateDouble(sa[2]),
                    truncateDouble(sa[3])));
        } else {
            stringAnswer = s;
            answerDisplay.setText("");
        }

        updateButtonLabelsAndVisibility(true);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        viewButton.setOnLongClickListener(l);
        getLocationButton.setOnLongClickListener(l);
        answerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        viewButton.cancelLongPress();
        getLocationButton.cancelLongPress();
        answerDisplay.cancelLongPress();
    }

    private boolean useMapsV2(final Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    @Override
    public void onButtonClick(int buttonId) {
        requestLocationPermissions((FormEntryActivity) getContext(), new PermissionListener() {
            @Override
            public void granted() {
                startGeoPoint();
            }

            @Override
            public void denied() {
            }
        });
    }

    private void startGeoPoint() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "recordLocation", "click",
                        getFormEntryPrompt().getIndex());
        Intent i;
        if (useMapsV2 && useMaps) {
            if (mapSDK.equals(GOOGLE_MAP_KEY)) {
                if (PlayServicesUtil.isGooglePlayServicesAvailable(getContext())) {
                    i = new Intent(getContext(), GeoPointMapActivity.class);
                } else {
                    PlayServicesUtil.showGooglePlayServicesAvailabilityErrorDialog(getContext());
                    return;
                }
            } else {
                i = new Intent(getContext(), GeoPointOsmMapActivity.class);
            }
        } else {
            i = new Intent(getContext(), GeoPointActivity.class);
        }

        if (stringAnswer != null && !stringAnswer.isEmpty()) {
            String[] sa = stringAnswer.split(" ");
            double[] gp = new double[4];
            gp[0] = Double.valueOf(sa[0]);
            gp[1] = Double.valueOf(sa[1]);
            gp[2] = Double.valueOf(sa[2]);
            gp[3] = Double.valueOf(sa[3]);
            i.putExtra(LOCATION, gp);
        }
        i.putExtra(READ_ONLY, readOnly);
        i.putExtra(DRAGGABLE_ONLY, draggable);
        i.putExtra(ACCURACY_THRESHOLD, accuracyThreshold);

        waitForData();

        ((Activity) getContext()).startActivityForResult(i, RequestCodes.LOCATION_CAPTURE);
    }
}