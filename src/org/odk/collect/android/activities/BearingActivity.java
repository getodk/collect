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

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

public class BearingActivity extends Activity implements SensorEventListener {
    private ProgressDialog mBearingDialog;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private static float[] mAccelerometer = null;
    private static float[] mGeomagnetic = null;

    private String mBearing = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.get_bearing));

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        setupBearingDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this, accelerometer);
        mSensorManager.unregisterListener(this, magnetometer);

        if (mBearingDialog != null && mBearingDialog.isShowing())
            mBearingDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        mBearingDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

    /**
     * Sets up the look and actions for the progress dialog while the compass is
     * searching.
     */
    private void setupBearingDialog() {
        Collect.getInstance().getActivityLogger()
                .logInstanceAction(this, "setupBearingDialog", "show");
        // dialog displayed while fetching bearing
        mBearingDialog = new ProgressDialog(this);
        DialogInterface.OnClickListener geopointButtonListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Collect.getInstance().getActivityLogger()
                                        .logInstanceAction(this, "acceptBearing", "OK");
                                returnBearing();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Collect.getInstance().getActivityLogger()
                                        .logInstanceAction(this, "cancelBearing", "cancel");
                                mBearing = null;
                                finish();
                                break;
                        }
                    }
                };

        // back button doesn't cancel
        mBearingDialog.setCancelable(false);
        mBearingDialog.setIndeterminate(true);
        mBearingDialog.setIcon(android.R.drawable.ic_dialog_info);
        mBearingDialog.setTitle(getString(R.string.getting_bearing));
        mBearingDialog.setMessage(getString(R.string.please_wait_long));
        mBearingDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.accept_bearing),
                geopointButtonListener);
        mBearingDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel_location),
                geopointButtonListener);
    }

    private void returnBearing() {
        if (mBearing != null) {
            Intent i = new Intent();
            i.putExtra(
                    FormEntryActivity.BEARING_RESULT, mBearing);
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
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mAccelerometer, mGeomagnetic);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // at this point, orientation contains the azimuth(direction),
                // pitch and roll values.
                double azimuth = 180 * orientation[0] / Math.PI;
                // double pitch = 180 * orientation[1] / Math.PI;
                // double roll = 180 * orientation[2] / Math.PI;
                double degrees = normalizeDegree(azimuth);
                mBearing = String.format("%.3f", degrees);
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
                mBearingDialog.setMessage("Dir: " + dir + " Bearing: " + mBearing);

            }
        }
    }

    private double normalizeDegree(double value) {
        if (value >= 0.0f && value <= 180.0f) {
            return value;
        } else {
            return 180 + (180 + value);
        }
    }

}
