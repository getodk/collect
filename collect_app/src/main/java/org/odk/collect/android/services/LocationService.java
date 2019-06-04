/*
 * Copyright (C) 2017 Smap Consulting Pty Ltd
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

package org.odk.collect.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.TraceUtilities;
import org.odk.collect.android.loaders.GeofenceEntry;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.utilities.Constants;
import org.odk.collect.android.utilities.NotificationUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import timber.log.Timber;

import static java.lang.StrictMath.abs;

/**
 * Created by neilpenman on 2018-01-11.
 */

/*
 * Respond to a notification from the server
 */
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks {

    Handler mHandler = new Handler();           // Background thread to check for enabling / disabling the location listener
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isRecordingLocation = false;
    private Timer mTimer;
    Location lastLocation = null;
    LocationManager locationManager;
    boolean enabledTracking = false;

    public LocationService() {
    }
    public LocationService(Context applicationContext) {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        Timber.i("======================= Start Location Service");

        if (mTimer == null) {
            mTimer = new Timer();
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mTimer.scheduleAtFixedRate(new CheckEnabledTimerTask(), 0, 60000);  // Peiodically check to see if location tracking is disabled

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        enabledTracking = sharedPreferences.getBoolean(GeneralKeys.KEY_SMAP_USER_LOCATION, false);

        startLocationUpdates();

        return START_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Timber.i("++++++++++Connected to provider");
        stopLocationUpdates();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.i("+++++++++++ Connection Suspended");
        stopLocationUpdates();
    }

    /*
     * Start recording locations
     */
    private void startLocationUpdates() {
        Timber.i("=================== Location Recording turned on");
        if(locationRequest == null) {
            locationRequest = LocationRequest.create();
        }
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(Constants.GPS_INTERVAL / 2);
        locationRequest.setInterval(Constants.GPS_INTERVAL);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            isRecordingLocation = true;
        } catch (SecurityException e) {
            Timber.i("%%%%%%%%%%%%%%%%%%%% location recording not permitted: ");
            isRecordingLocation = false;
        }
    }

    /*
     * Stop recoding locations
     */
    private void stopLocationUpdates() {
        Timber.i("=================== Location Recording turned on");
        if(fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        isRecordingLocation = false;
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onLocationChanged(Location location) {

        if(isValidLocation(location) && isAccurateLocation(location)) {

            Timber.i("+++++++++++++++++++++++++++++ location received");
            Collect.getInstance().setLocation(location);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("locationChanged"));  // update map

            /*
             * Test for geofence change if the user has moved more than the minimum distance
             */
            ArrayList<GeofenceEntry> geofences = Collect.getInstance().getGeofences();
            if(geofences.size() > 0 && (lastLocation == null || location.distanceTo(lastLocation) > Constants.GPS_DISTANCE)) {
                boolean refresh = false;
                boolean notify = false;
                for(GeofenceEntry gfe : geofences) {
                    double yDistance = abs(location.getLatitude() - gfe.location.getLatitude()) * 111111.1;     // lattitude difference in meters
                    if(gfe.in) {                                                        // Currently inside
                        if (location.distanceTo(gfe.location) > gfe.showDist) {         // detailed check only
                            refresh = true;
                        }
                    } else {
                        if(yDistance < gfe.showDist) {                                      // Currently outside do rough check first
                            if (location.distanceTo(gfe.location) < gfe.showDist) {
                                refresh = true;
                                notify = true;
                                break;      // No need to check more we have a notify and a refresh
                            }
                        }
                    }
                }
                if(refresh) {
                    Intent intent = new Intent("org.smap.smapTask.refresh");
                    LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);
                    Timber.i("######## send org.smap.smapTask.refresh from location service");  // smap
                }
                if(notify) {
                    NotificationUtils.showNotification(null,
                            NotificationActivity.NOTIFICATION_ID,
                            R.string.app_name,
                            getString(R.string.smap_geofence_tasks), false);
                }

            }

            /*
             * Save the location in the database - deprecate better to send location immediately to server if we do this
             */
            if (enabledTracking) {
                if(lastLocation == null || location.distanceTo(lastLocation) > Constants.GPS_DISTANCE) {
                    Timber.i("^^^^^^^^^^^^^^^^^^^^^^^^^^ insert point");
                    TraceUtilities.insertPoint(location);
                    lastLocation = location;
                }
            }
        }
    }

    /*
     * Check to see if this is a valid location
     */
    private boolean isValidLocation(Location location) {
        boolean valid = true;
        if(location == null || Math.abs(location.getLatitude()) > 90
                || Math.abs(location.getLongitude()) > 180) {
            valid = false;
        }

        // Return false if the location is 0 0, more likely than not this is a bad location
        if(Math.abs(location.getLongitude()) == 0.0 && Math.abs(location.getLongitude()) == 0.0) {
            valid = false;
        }

        return valid;
    }

    /*
     * Check to see if this is a valid location
     */
    private boolean isAccurateLocation(Location location) {

        boolean accurate = true;
        if (!location.hasAccuracy() || location.getAccuracy() >= Constants.GPS_ACCURACY) {
            Timber.i("Ignore location. Poor accuracy.");
            accurate = false;
        }
        return accurate;
    }

    /*
     * Run a a periodic query to see if the user settings have changes
     */
    class CheckEnabledTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    Timber.i("=================== Periodic check for user settings ");
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
                    enabledTracking = sharedPreferences.getBoolean(GeneralKeys.KEY_SMAP_USER_LOCATION, false);

                    // Restart location monitoring - Incase pemission was disabled and then reenabled
                    stopLocationUpdates();
                    startLocationUpdates();
                }
            });

        }
    }



}
