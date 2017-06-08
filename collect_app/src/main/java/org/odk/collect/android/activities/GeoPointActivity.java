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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.InfoLogger;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.GeoPointWidget;

import java.text.DecimalFormat;
import java.util.List;

public class GeoPointActivity extends AppCompatActivity implements LocationListener {

    private static final String LOCATION_COUNT = "locationCount";

    private ProgressDialog locationDialog;
    private LocationManager locationManager;
    private Location location;
    private boolean gpsOn = false;
    private boolean networkOn = false;
    private double locationAccuracy;
    private int locationCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (savedInstanceState != null) {
            locationCount = savedInstanceState.getInt(LOCATION_COUNT);
        }

        Intent intent = getIntent();

        locationAccuracy = GeoPointWidget.DEFAULT_LOCATION_ACCURACY;
        if (intent != null && intent.getExtras() != null) {
            if (intent.hasExtra(GeoPointWidget.ACCURACY_THRESHOLD)) {
                locationAccuracy = intent.getDoubleExtra(GeoPointWidget.ACCURACY_THRESHOLD,
                        GeoPointWidget.DEFAULT_LOCATION_ACCURACY);
            }
        }

        setTitle(getString(R.string.get_location));
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // make sure we have a good location provider before continuing
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                gpsOn = true;
            }
            if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                networkOn = true;
            }
        }
        if (!gpsOn && !networkOn) {
            ToastUtils.showShortToast(R.string.provider_disabled_error);
            Intent onGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(onGPSIntent);
            finish();
        }

        if (gpsOn) {
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null) {
                InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis()
                        + " lastKnownLocation(GPS) lat: "
                        + loc.getLatitude() + " long: "
                        + loc.getLongitude() + " acc: "
                        + loc.getAccuracy());
            } else {
                InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis()
                        + " lastKnownLocation(GPS) null location");
            }
        }

        if (networkOn) {
            Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc != null) {
                InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis()
                        + " lastKnownLocation(Network) lat: "
                        + loc.getLatitude() + " long: "
                        + loc.getLongitude() + " acc: "
                        + loc.getAccuracy());
            } else {
                InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis()
                        + " lastKnownLocation(Network) null location");
            }
        }

        setupLocationDialog();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LOCATION_COUNT, locationCount);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stops the GPS. Note that this will turn off the GPS if the screen goes to sleep.
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }

        // We're not using managed dialogs, so we have to dismiss the dialog to prevent it from
        // leaking memory.
        if (locationDialog != null && locationDialog.isShowing()) {
            locationDialog.dismiss();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (locationManager != null) {
            if (gpsOn) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
            if (networkOn) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
        }
        if (locationDialog != null) {
            locationDialog.show();
        }
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
     * Sets up the look and actions for the progress dialog while the GPS is searching.
     */
    private void setupLocationDialog() {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "setupLocationDialog",
                "show");
        // dialog displayed while fetching gps location
        locationDialog = new ProgressDialog(this);
        DialogInterface.OnClickListener geoPointButtonListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Collect.getInstance().getActivityLogger().logInstanceAction(this,
                                        "acceptLocation", "OK");
                                returnLocation();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Collect.getInstance().getActivityLogger().logInstanceAction(this,
                                        "cancelLocation", "cancel");
                                location = null;
                                finish();
                                break;
                        }
                    }
                };

        // back button doesn't cancel
        locationDialog.setCancelable(false);
        locationDialog.setIndeterminate(true);
        locationDialog.setIcon(android.R.drawable.ic_dialog_info);
        locationDialog.setTitle(getString(R.string.getting_location));
        locationDialog.setMessage(getString(R.string.please_wait_long));
        locationDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.save_point),
                geoPointButtonListener);
        locationDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel_location),
                geoPointButtonListener);
    }


    private void returnLocation() {
        if (location != null) {
            Intent i = new Intent();
            i.putExtra(
                    FormEntryActivity.LOCATION_RESULT,
                    location.getLatitude() + " " + location.getLongitude() + " "
                            + location.getAltitude() + " " + location.getAccuracy());
            setResult(RESULT_OK, i);
        }
        finish();
    }


    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        if (this.location != null) {
            // Bug report: cached GeoPoint is being returned as the first value.
            // Wait for the 2nd value to be returned, which is hopefully not cached?
            ++locationCount;
            InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis()
                    + " onLocationChanged(" + locationCount + ") lat: "
                    + this.location.getLatitude() + " long: "
                    + this.location.getLongitude() + " acc: "
                    + this.location.getAccuracy());

            if (locationCount > 1) {
                locationDialog.setMessage(getString(R.string.location_provider_accuracy,
                        this.location.getProvider(), truncateDouble(this.location.getAccuracy())));

                if (this.location.getAccuracy() <= locationAccuracy) {
                    returnLocation();
                }
            }
        } else {
            InfoLogger.geolog("GeoPointActivity: " + System.currentTimeMillis()
                    + " onLocationChanged(" + locationCount + ") null location");
        }
    }

    private String truncateDouble(float number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }


    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                if (location != null) {
                    locationDialog.setMessage(getString(R.string.location_accuracy,
                            location.getAccuracy()));
                }
                break;
            case LocationProvider.OUT_OF_SERVICE:
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                break;
        }
    }

}
