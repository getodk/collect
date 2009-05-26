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

package org.google.android.odk.widgets;

import org.google.android.odk.PromptElement;
import org.google.android.odk.R;
import org.google.android.odk.SharedConstants;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IAnswerData;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * GeoPointWidget is the widget that allows the user to get GPS readings.
 * 
 * @author Carl Hartung
 * @author Yaw Anokwa
 */
public class GeoPointWidget extends LinearLayout implements IQuestionWidget {

    private Button mActionButton;
    private TextView mStringAnswer;

    private ProgressDialog mLocationDialog;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;


    public GeoPointWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        mStringAnswer.setText(null);
    }


    public IAnswerData getAnswer() {
        stopGPS();
        String s = mStringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        }

        if (s != null) {
            try {
                // segment lat and lon
                String[] sga = s.replace("lat: ", "").replace("\nlon: ", ",").split("[,]");
                double gp[] = new double[2];
                gp[0] = Double.valueOf(sga[0]).doubleValue();
                gp[1] = Double.valueOf(sga[1]).doubleValue();
                return new GeoPointData(gp);
            } catch (Exception NumberFormatException) {
                return null;
            }
        }
        return null;
    }


    public void buildView(PromptElement prompt) {
        setOrientation(LinearLayout.VERTICAL);

        mActionButton = new Button(getContext());
        mActionButton.setPadding(20, 20, 20, 20);
        mActionButton.setText(getContext().getString(R.string.get_location));
        mActionButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.TEXTSIZE);
        mActionButton.setEnabled(!prompt.isReadonly());

        mStringAnswer = new TextView(getContext());

        String s = prompt.getAnswerText();
        if (s != null) {
            mStringAnswer.setText(s);

            mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.TEXTSIZE);

            // string manipulation for readability
            String str = (String) mStringAnswer.getText();
            if (!str.equals("")) {
                mStringAnswer.setText("lat: " + str.replace(",", "\nlon: "));
            }
            mStringAnswer.setGravity(Gravity.CENTER);
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
                mLocationDialog.setMessage(getContext().getString(R.string.getting_location));
                mLocationDialog.setButton(getContext().getString(R.string.cancel),
                        geopointButtonListener);
                mLocationDialog.show();
            }
        });

        // finish complex layout
        addView(mActionButton);
        addView(mStringAnswer);

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
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3,
                mLocationListener);
    }


    /**
     * Stop listening to any updates from GPS
     */
    private void stopGPS() {
        if (mLocationDialog != null && mLocationDialog.isShowing()) {
            mLocationDialog.dismiss();
            if (mLocation != null) {
                mStringAnswer.setText("lat: " + mLocation.getLatitude() + "\nlon: "
                        + mLocation.getLongitude());
            }
        }
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

}
