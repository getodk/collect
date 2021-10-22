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

package org.odk.collect.geo;

import static org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY;
import static org.odk.collect.geo.GeoActivityUtils.requireLocationPermissions;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.androidshared.ui.ToastUtils;
import org.odk.collect.externalapp.ExternalAppUtils;
import org.odk.collect.location.GoogleFusedLocationClient;
import org.odk.collect.location.LocationClient;
import org.odk.collect.location.LocationClientProvider;
import org.odk.collect.strings.localization.LocalizedActivity;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class GeoPointActivity extends LocalizedActivity implements LocationListener,
        LocationClient.LocationClientListener, GpsStatus.Listener {

    public static final String EXTRA_ACCURACY_THRESHOLD = "accuracyThreshold";

    // Default values for requesting Location updates.
    private static final long LOCATION_UPDATE_INTERVAL = 100;
    private static final long LOCATION_FASTEST_UPDATE_INTERVAL = 50;

    private static final String LOCATION_COUNT = "locationCount";
    private static final String START_TIME = "startTime";
    private static final String NUMBER_OF_AVAILABLE_SATELLITES = "numberOfAvailableSatellites";

    private AlertDialog locationDialog;

    private LocationClient locationClient;
    private Location location;

    private double targetAccuracy = Double.MAX_VALUE;

    private int locationCount;
    private int numberOfAvailableSatellites;

    private long startTime = System.currentTimeMillis();

    private String dialogMessage;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireLocationPermissions(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (savedInstanceState != null) {
            locationCount = savedInstanceState.getInt(LOCATION_COUNT);
            startTime = savedInstanceState.getLong(START_TIME);
            numberOfAvailableSatellites = savedInstanceState.getInt(NUMBER_OF_AVAILABLE_SATELLITES);
        }

        setTitle(getString(R.string.get_location));

        locationClient = LocationClientProvider.getClient(this,
                () -> new GoogleFusedLocationClient(getApplication()), GoogleApiAvailability
                        .getInstance());
        if (locationClient.canSetUpdateIntervals()) {
            locationClient.setUpdateIntervals(LOCATION_UPDATE_INTERVAL, LOCATION_FASTEST_UPDATE_INTERVAL);
        }

        Intent intent = getIntent();
        targetAccuracy = intent.getDoubleExtra(EXTRA_ACCURACY_THRESHOLD, Double.MAX_VALUE);
        locationClient.setRetainMockAccuracy(intent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, false));

        locationClient.setListener(this);

        setupLocationDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();

        locationClient.start();

        if (locationDialog != null) {
            locationDialog.show();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateDialogMessage();
                }
            }, 0, 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.removeGpsStatusListener(this);
        }

        locationClient.stop();
        locationClient.setListener(null);

        if (timer != null) {
            timer.cancel();
        }

        // We're not using managed dialogs, so we have to dismiss the dialog to prevent it from
        // leaking memory.
        if (locationDialog != null && locationDialog.isShowing()) {
            locationDialog.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LOCATION_COUNT, locationCount);
        outState.putLong(START_TIME, startTime);
    }

    //region LocationClientListener:

    @SuppressLint("MissingPermission") // Checking Permissions handled in constructor
    @Override
    public void onClientStart() {
        locationClient.requestLocationUpdates(this);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.addGpsStatusListener(this);
        }

        if (locationClient.isLocationAvailable()) {
            logLastLocation();
        } else {
            finishOnError();
        }
    }

    @Override
    public void onClientStartFailure() {
        finishOnError();
    }

    @Override
    public void onClientStop() {
    }

    //endregion

    /**
     * Sets up the look and actions for the progress dialog while the GPS is searching.
     */
    private void setupLocationDialog() {
        // dialog displayed while fetching gps location
        locationDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.getting_location)
                .setView(R.layout.geopoint_dialog)
                .setCancelable(false) // taping outside the dialog doesn't cancel
                .create();

        dialogMessage = getString(R.string.please_wait_long);

        DialogInterface.OnClickListener geoPointButtonListener =
                (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            returnLocation();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            location = null;
                            finish();
                            break;
                    }
                };
        locationDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.save_point),
                geoPointButtonListener);
        locationDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel_location),
                geoPointButtonListener);
    }

    private void logLastLocation() {
        Location loc = locationClient.getLastLocation();

        if (loc != null) {
            Timber.i("lastKnownLocation() lat: %f long: %f acc: %f", loc.getLatitude(), loc.getLongitude(), loc.getAccuracy());

        } else {
            Timber.i("lastKnownLocation() null location");
        }
    }

    private void returnLocation() {
        if (location != null) {
            ExternalAppUtils.returnSingleValue(this, getResultStringForLocation(location));
        } else {
            finish();
        }
    }

    private void finishOnError() {
        ToastUtils.showShortToast(this, R.string.provider_disabled_error);
        Intent onGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        startActivity(onGPSIntent);
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;

        if (location != null) {
            // Bug report: cached GeoPoint is being returned as the first value.
            // Wait for the 2nd value to be returned, which is hopefully not cached?
            ++locationCount;
            Timber.i("onLocationChanged(%d) location: %s", locationCount, location);

            if (locationCount > 1) {
                if (location.getAccuracy() <= targetAccuracy) {
                    returnLocation();
                }
            }

            dialogMessage = getAccuracyMessage(location) + "\n\n" + getProviderMessage(location);
            updateDialogMessage();
        } else {
            Timber.i("onLocationChanged(%d)", locationCount);
        }
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                GpsStatus status = locationManager.getGpsStatus(null);
                Iterable<GpsSatellite> satellites = status.getSatellites();
                int satellitesNumber = 0;
                for (GpsSatellite satellite : satellites) {
                    if (satellite.usedInFix()) {
                        satellitesNumber++;
                    }
                }

                numberOfAvailableSatellites = satellitesNumber;
                updateDialogMessage();
            }
        }
    }

    public String getAccuracyMessage(@NonNull Location location) {
        return getString(R.string.location_accuracy, truncateDouble(location.getAccuracy()));
    }

    public String getProviderMessage(@NonNull Location location) {
        return getString(R.string.location_provider, GeoUtils.capitalizeGps(location.getProvider()));
    }

    public String getResultStringForLocation(@NonNull Location location) {
        return GeoUtils.formatLocationResultString(location);
    }

    private String truncateDouble(float number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(number);
    }

    public String getDialogMessage() {
        return dialogMessage;
    }

    private void updateDialogMessage() {
        String timeElapsed = DateUtils.formatElapsedTime((System.currentTimeMillis() - startTime) / 1000);
        String locationMetadata = getString(R.string.location_metadata, numberOfAvailableSatellites, timeElapsed);
        runOnUiThread(() -> {
            String message = dialogMessage + "\n\n" + locationMetadata;
            ((TextView) locationDialog.findViewById(R.id.message)).setText(message);
        });
    }
}
