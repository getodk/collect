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

package org.odk.collect.android.activities;

import org.odk.collect.android.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.Toast;

import java.text.DecimalFormat;

public class GeoPointActivity extends Activity implements LocationListener {

    private ProgressDialog mLocationDialog;
    private LocationManager mLocationManager;
    private Location mLocation;

    // default location accuracy
    private static double LOCATION_ACCURACY = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.get_location));

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setupLocationDialog();
    }


    @Override
    protected void onPause() {
        super.onPause();

        // stops the GPS. Note that this will turn off the GPS if the screen goes to sleep.
        mLocationManager.removeUpdates(this);

        // We're not using managed dialogs, so we have to dismiss the dialog to prevent it from
        // leaking memory.
        if (mLocationDialog != null && mLocationDialog.isShowing())
            mLocationDialog.dismiss();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        mLocationDialog.show();
    }


    /**
     * Sets up the look and actions for the progress dialog while the GPS is searching.
     */
    private void setupLocationDialog() {
        // dialog displayed while fetching gps location
        mLocationDialog = new ProgressDialog(this);
        DialogInterface.OnClickListener geopointButtonListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON1:
                            returnLocation();
                            break;
                        case DialogInterface.BUTTON2:
                            mLocation = null;
                            finish();
                            break;
                    }
                }
            };

        // back button doesn't cancel
        mLocationDialog.setCancelable(false);
        mLocationDialog.setIndeterminate(true);
        mLocationDialog.setIcon(android.R.drawable.ic_dialog_info);
        mLocationDialog.setTitle(getString(R.string.getting_location));
        mLocationDialog.setMessage(getString(R.string.please_wait_long));
        mLocationDialog.setButton(DialogInterface.BUTTON1, getString(R.string.accept_location),
            geopointButtonListener);
        mLocationDialog.setButton(DialogInterface.BUTTON2, getString(R.string.cancel_location),
            geopointButtonListener);
    }


    private void returnLocation() {
        if (mLocation != null) {
            Intent i = new Intent();
            i.putExtra(
                FormEntryActivity.LOCATION_RESULT,
                mLocation.getLatitude() + " " + mLocation.getLongitude() + " "
                        + mLocation.getAltitude() + " " + mLocation.getAccuracy());
            setResult(RESULT_OK, i);
        }
        finish();
    }


    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        mLocationDialog.setMessage(getString(R.string.location_provider_accuracy,
            mLocation.getProvider(), truncateDouble(mLocation.getAccuracy())));

        if (mLocation.getAccuracy() <= LOCATION_ACCURACY) {
            returnLocation();
        }
    }


    private String truncateDouble(float number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }


    @Override
    public void onProviderDisabled(String provider) {
        if (!(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            Toast.makeText(this, getString(R.string.provider_disabled_error), Toast.LENGTH_SHORT)
                    .show();
            finish();
        }
    }


    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                mLocationDialog.setMessage(getString(R.string.location_accuracy,
                    mLocation.getAccuracy()));
                break;
            case LocationProvider.OUT_OF_SERVICE:
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                break;
        }
    }

}
