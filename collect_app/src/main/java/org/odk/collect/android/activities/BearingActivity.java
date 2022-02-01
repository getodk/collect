/*
 * Copyright (C) 2013 Nafundi
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

package org.odk.collect.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.dialogs.BearingProgressDialog;
import org.odk.collect.androidshared.ui.DialogFragmentUtils;

import java.util.Locale;

public class BearingActivity extends CollectAbstractActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private static float[] mAccelerometer;
    private static float[] mGeomagnetic;

    private String bearingDecimal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.get_bearing));

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnetometer);
        DialogFragmentUtils.dismissDialog(BearingProgressDialog.class, getSupportFragmentManager());
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        setupBearingDialog();
    }

    /**
     * Sets up the look and actions for the progress dialog while the compass is
     * searching.
     */
    private void setupBearingDialog() {
        DialogInterface.OnClickListener geopointButtonListener =
                (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            returnBearing();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            bearingDecimal = null;
                            finish();
                            break;
                    }
                };

        BearingProgressDialog dialog = new BearingProgressDialog(geopointButtonListener);
        DialogFragmentUtils.showIfNotShowing(dialog, BearingProgressDialog.class, getSupportFragmentManager());
    }

    private void returnBearing() {
        if (bearingDecimal != null) {
            Intent i = new Intent();
            i.putExtra(
                    FormEntryActivity.ANSWER_KEY, bearingDecimal);
            setResult(RESULT_OK, i);
        }
        finish();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // onSensorChanged gets called for each sensor so we have to remember
        // the values
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccelerometer = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }

        if (mAccelerometer != null && mGeomagnetic != null) {
            float[] rot = new float[9];
            float[] inclination = new float[9];
            boolean success = SensorManager.getRotationMatrix(rot, inclination, mAccelerometer,
                    mGeomagnetic);

            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rot, orientation);
                // at this point, orientation contains the azimuth(direction),
                // pitch and roll values.
                double azimuth = 180 * orientation[0] / Math.PI;
                // double pitch = 180 * orientation[1] / Math.PI;
                // double roll = 180 * orientation[2] / Math.PI;
                double degrees = normalizeDegrees(azimuth);
                bearingDecimal = formatDegrees(degrees);

                String dir = "N";
                if ((degrees > 0 && degrees <= 22.5) || degrees > 337.5) {
                    dir = "N";
                } else if (degrees > 22.5 && degrees <= 67.5) {
                    dir = "NE";
                } else if (degrees > 67.5 && degrees <= 112.5) {
                    dir = "E";
                } else if (degrees > 112.5 && degrees <= 157.5) {
                    dir = "SE";
                } else if (degrees > 157.5 && degrees <= 222.5) {
                    dir = "S";
                } else if (degrees > 222.5 && degrees <= 247.5) {
                    dir = "SW";
                } else if (degrees > 247.5 && degrees <= 292.5) {
                    dir = "W";
                } else if (degrees > 292.5 && degrees <= 337.5) {
                    dir = "NW";
                }
                BearingProgressDialog existingDialog = (BearingProgressDialog) getSupportFragmentManager()
                        .findFragmentByTag(BearingProgressDialog.class.getName());

                if (existingDialog != null) {
                    existingDialog.setMessage(getString(R.string.direction, dir)
                            + "\n" + getString(R.string.bearing, degrees));
                }
            }
        }
    }

    public static String formatDegrees(double degrees) {
        return String.format(Locale.US, "%.3f", degrees);
    }

    public static double normalizeDegrees(double value) {
        if (value >= 0.0f && value <= 180.0f) {
            return value;
        } else {
            return 180 + (180 + value);
        }
    }
}
