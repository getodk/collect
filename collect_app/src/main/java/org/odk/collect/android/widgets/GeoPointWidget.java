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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
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
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.PlayServicesUtil;

import java.text.DecimalFormat;

/**
 * GeoPointWidget is the widget that allows the user to get GPS readings.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Jon Nordling (jonnordling@gmail.com)
 */
public class GeoPointWidget extends QuestionWidget implements IBinaryWidget {
    public static final String LOCATION = "gp";
    public static final String ACCURACY_THRESHOLD = "accuracyThreshold";
    public static final String READ_ONLY = "readOnly";
    public static final String DRAGGABLE_ONLY = "draggable";

    public static final double DEFAULT_LOCATION_ACCURACY = 5.0;

    private Button mGetLocationButton;
    private Button mViewButton;
    private SharedPreferences sharedPreferences;
    private String mapSDK;
    private String GOOGLE_MAP_KEY = "google_maps";
    private String OSM_MAP_KEY = "osmdroid";
    private TextView mStringAnswer;
    private TextView mAnswerDisplay;
    private final boolean mReadOnly;
    private final boolean mUseMapsV2;
    private boolean mUseMaps;
    private String mAppearance;
    private double mAccuracyThreshold;
    private boolean draggable = true;

    public GeoPointWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        // Determine the activity threshold to use
        String acc = prompt.getQuestion().getAdditionalAttribute(null, ACCURACY_THRESHOLD);
        if (acc != null && acc.length() != 0) {
            mAccuracyThreshold = Double.parseDouble(acc);
        } else {
            mAccuracyThreshold = DEFAULT_LOCATION_ACCURACY;
        }

        // Determine whether or not to use the plain, maps, or mapsV2 activity
        mAppearance = prompt.getAppearanceHint();
        // use mapsV2 if it is available and was requested;
        mUseMapsV2 = useMapsV2(context);
        if (mAppearance != null && mAppearance.equalsIgnoreCase("placement-map") && mUseMapsV2) {
            draggable = true;
            mUseMaps = true;
        } else if (mAppearance != null && mAppearance.equalsIgnoreCase("maps") && mUseMapsV2) {
            draggable = false;
            mUseMaps = true;
        } else {
            mUseMaps = false;
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mapSDK = sharedPreferences.getString(PreferenceKeys.KEY_MAP_SDK, GOOGLE_MAP_KEY);


        mReadOnly = prompt.isReadOnly();

        // assemble the widget...
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        mStringAnswer = new TextView(getContext());
        mStringAnswer.setId(QuestionWidget.newUniqueId());
        mAnswerDisplay = new TextView(getContext());
        mAnswerDisplay.setId(QuestionWidget.newUniqueId());
        mAnswerDisplay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mAnswerDisplay.setGravity(Gravity.CENTER);

        // setup play button
        mViewButton = new Button(getContext());
        mViewButton.setId(QuestionWidget.newUniqueId());
        mViewButton.setText(getContext().getString(R.string.get_point));
        mViewButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mViewButton.setPadding(20, 20, 20, 20);
        mViewButton.setLayoutParams(params);

        mGetLocationButton = new Button(getContext());
        mGetLocationButton.setId(QuestionWidget.newUniqueId());
        mGetLocationButton.setPadding(20, 20, 20, 20);
        mGetLocationButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mGetLocationButton.setEnabled(!prompt.isReadOnly());
        mGetLocationButton.setLayoutParams(params);

        // when you press the button
        mGetLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "recordLocation", "click",
                                mPrompt.getIndex());
                Intent i = null;
                if (mUseMapsV2 && mUseMaps) {
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

                String s = mStringAnswer.getText().toString();
                if (s.length() != 0) {
                    String[] sa = s.split(" ");
                    double gp[] = new double[4];
                    gp[0] = Double.valueOf(sa[0]).doubleValue();
                    gp[1] = Double.valueOf(sa[1]).doubleValue();
                    gp[2] = Double.valueOf(sa[2]).doubleValue();
                    gp[3] = Double.valueOf(sa[3]).doubleValue();
                    i.putExtra(LOCATION, gp);
                }
                i.putExtra(READ_ONLY, mReadOnly);
                i.putExtra(DRAGGABLE_ONLY, draggable);
                i.putExtra(ACCURACY_THRESHOLD, mAccuracyThreshold);
                Collect.getInstance().getFormController()
                        .setIndexWaitingForData(mPrompt.getIndex());
                ((Activity) getContext()).startActivityForResult(i,
                        FormEntryActivity.LOCATION_CAPTURE);
            }
        });

        // finish complex layout
        // control what gets shown with setVisibility(View.GONE)
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(mGetLocationButton);
        answerLayout.addView(mViewButton);
        answerLayout.addView(mAnswerDisplay);
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
        // BUT for mapsV2, we only show the mGetLocationButton, altering its text.
        // for maps, we show the view button.

        if (mUseMapsV2 && mUseMaps) {
            // show the GetLocation button
            mGetLocationButton.setVisibility(View.VISIBLE);
            // hide the view button
            mViewButton.setVisibility(View.GONE);
            if (mReadOnly) {
                //READ_ONLY View
                mGetLocationButton.setText(
                        getContext().getString(R.string.geopoint_view_read_only));
            } else {
                String s = mStringAnswer.getText().toString();
                if (s.length() != 0) {
                    mGetLocationButton.setText(
                            getContext().getString(R.string.view_change_location));
                } else {
                    mGetLocationButton.setText(getContext().getString(R.string.get_point));
                }
            }
        } else {
            // if it is read-only, hide the get-location button...
            if (mReadOnly) {
                mGetLocationButton.setVisibility(View.GONE);
            } else {
                mGetLocationButton.setVisibility(View.VISIBLE);
                mGetLocationButton.setText(getContext().getString(
                        dataAvailable ? R.string.get_point : R.string.get_point));
            }

            if (mUseMaps) {
                // show the view button
                mViewButton.setVisibility(View.VISIBLE);
                mViewButton.setEnabled(dataAvailable);
            } else {
                mViewButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void clearAnswer() {
        mStringAnswer.setText(null);
        mAnswerDisplay.setText(null);
        updateButtonLabelsAndVisibility(false);
    }

    @Override
    public IAnswerData getAnswer() {
        String s = mStringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            try {
                // segment lat and lon
                String[] sa = s.split(" ");
                double gp[] = new double[4];
                gp[0] = Double.valueOf(sa[0]).doubleValue();
                gp[1] = Double.valueOf(sa[1]).doubleValue();
                gp[2] = Double.valueOf(sa[2]).doubleValue();
                gp[3] = Double.valueOf(sa[3]).doubleValue();

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
        String degree = location.substring(0, location.indexOf("."))
                + degreeSign;
        location = "0." + location.substring(location.indexOf(".") + 1);
        double temp = Double.valueOf(location) * 60;
        location = Double.toString(temp);
        String mins = location.substring(0, location.indexOf(".")) + "'";

        location = "0." + location.substring(location.indexOf(".") + 1);
        temp = Double.valueOf(location) * 60;
        location = Double.toString(temp);
        String secs = location.substring(0, location.indexOf(".")) + '"';
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
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setBinaryData(Object answer) {
        String s = (String) answer;
        if (!s.equals("") || s == null) {
            mStringAnswer.setText(s);
            String[] sa = s.split(" ");
            mAnswerDisplay.setText(String.format(getContext().getString(R.string.gps_result),
                    formatGps(Double.parseDouble(sa[0]), "lat"),
                    formatGps(Double.parseDouble(sa[1]), "lon"), truncateDouble(sa[2]),
                    truncateDouble(sa[3])));
        } else {
            mStringAnswer.setText(s);
            mAnswerDisplay.setText("");

        }
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
        updateButtonLabelsAndVisibility(true);
    }

    @Override
    public boolean isWaitingForBinaryData() {
        return mPrompt.getIndex().equals(
                Collect.getInstance().getFormController()
                        .getIndexWaitingForData());
    }

    @Override
    public void cancelWaitingForBinaryData() {
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mViewButton.setOnLongClickListener(l);
        mGetLocationButton.setOnLongClickListener(l);
        mStringAnswer.setOnLongClickListener(l);
        mAnswerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mViewButton.cancelLongPress();
        mGetLocationButton.cancelLongPress();
        mStringAnswer.cancelLongPress();
        mAnswerDisplay.cancelLongPress();
    }

    private boolean useMapsV2(final Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }
}