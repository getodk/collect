/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.logic.PromptElement;


/**
 * GeoPointWidget is the widget that allows the user to get GPS readings.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class GeoPointWidget extends LinearLayout implements IQuestionWidget {

    private Button mActionButton;
    private TextView mStringAnswer;
    private TextView mAnswerDisplay;

    private ProgressDialog mLocationDialog;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;


    public GeoPointWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        mStringAnswer.setText(null);
        mAnswerDisplay.setText(null);
    }


    public IAnswerData getAnswer() {
        stopGPS();
        String s = mStringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            try {
                // segment lat and lon
                String[] sa = s.split(" ");
                double gp[] = new double[2];
                gp[0] = Double.valueOf(sa[0]).doubleValue();
                gp[1] = Double.valueOf(sa[1]).doubleValue();
                return new GeoPointData(gp);
            } catch (Exception NumberFormatException) {
                return null;
            }
        }
    }


    public void buildView(PromptElement prompt) {

        setOrientation(LinearLayout.VERTICAL);

        mActionButton = new Button(getContext());
        mActionButton.setPadding(20, 20, 20, 20);
        mActionButton.setText(getContext().getString(R.string.get_location));
        mActionButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, GlobalConstants.APPLICATION_FONTSIZE);
        mActionButton.setEnabled(!prompt.isReadonly());

        mStringAnswer = new TextView(getContext());

        mAnswerDisplay = new TextView(getContext());
        mAnswerDisplay.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                GlobalConstants.APPLICATION_FONTSIZE - 1);
        mAnswerDisplay.setGravity(Gravity.CENTER);

        String s = prompt.getAnswerText();
        if (s != null && !s.equals("")) {
            setAnswer(s);
        }

        // when you press the button
        mActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startGPS();

                // dialog displayed while fetching gps location
                mLocationDialog = new ProgressDialog(getContext());
                DialogInterface.OnClickListener geopointButtonListener =
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // on cancel, stop gps
                                stopGPS();
                            }
                        };

                // back button doesn't cancel
                mLocationDialog.setCancelable(false);
                mLocationDialog.setIndeterminate(true);
                mLocationDialog.setTitle(getContext().getString(R.string.getting_location));
                mLocationDialog.setMessage(getContext().getString(R.string.please_wait));
                mLocationDialog.setButton(getContext().getString(R.string.cancel),
                        geopointButtonListener);
                mLocationDialog.show();
            }
        });

        // finish complex layout
        addView(mActionButton);
        addView(mAnswerDisplay);

    }


    private void setAnswer(String s) {
        mStringAnswer.setText(s);

        String[] sa = s.split(" ");
        mAnswerDisplay.setText("Lat: " + sa[0] + "\n" + "Lon: " + sa[1]);
    }


    /**
     * Create location manager and listener.
     */
    private void startGPS() {
        mLocationManager =
                (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager != null) {
            mLocationListener = new LocationListener() {

                // if location has changed, update location
                public void onLocationChanged(Location location) {
                    mLocation = location;
                    stopGPS();
                }


                // close gps dialogs, alert user, stop gps
                public void onProviderDisabled(String provider) {
                    stopGPS();
                    Toast
                            .makeText(getContext(),
                                    getContext().getString(R.string.gps_disabled_error),
                                    Toast.LENGTH_SHORT).show();
                }


                public void onProviderEnabled(String provider) {
                }


                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
            };
        }

        // start listening for changes
        if (mLocationManager != null) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3,
                    mLocationListener);
        }

    }


    /**
     * Stop listening to any updates from GPS
     */
    private void stopGPS() {

        if (mLocationDialog != null && mLocationDialog.isShowing()) {
            mLocationDialog.dismiss();
            if (mLocation != null) {
                setAnswer(mLocation.getLatitude() + " " + mLocation.getLongitude());
            }
        }
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }


    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

}
