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

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.TraceUtilities;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.utilities.Constants;
import org.odk.collect.android.utilities.NotificationUtils;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

/**
 * Created by neilpenman on 2018-01-11.
 */

/*
 * Respond to a notification from the server
 */
public class LocationService extends Service {

    Handler mHandler = new Handler();           // Background thread to check for enabling / disabling the location listener
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isRecordingLocation = false;
    private Timer mTimer;
    Location lastLocation = null;
    LocationManager locationManager;

    public LocationService(Context applicationContext) {
        super();
    }

    public LocationService() {
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

        return START_STICKY;
    }

    class CheckEnabledTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
                    boolean enabled = sharedPreferences.getBoolean(GeneralKeys.KEY_SMAP_USER_LOCATION, false);

                    Timber.i("=================== Checking location recording: " + enabled + " : " + isRecordingLocation);
                    if (enabled == isRecordingLocation) {
                        // No change however TODO may need to restart recording if GPS was turned off previously
                    } else {

                        NotificationManager mNotifyMgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                        if (enabled) {
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
                            } catch (SecurityException e) {
                                Timber.i("%%%%%%%%%%%%%%%%%%%% location recording not permitted: ");
                                isRecordingLocation = false;
                            }
                            isRecordingLocation = true;

                            /*
                             * Notify the user
                             */
                            NotificationUtils.showNotification(null,
                                    NotificationActivity.NOTIFICATION_ID,
                                    R.string.app_name,
                                    getString(R.string.smap_location_tracking), false);     // smap add start

                        } else {
                            Timber.i("=================== Location Recording turned off");
                            if(fusedLocationClient != null) {
                                fusedLocationClient.removeLocationUpdates(locationCallback);
                            }
                            mNotifyMgr.cancel(NotificationActivity.LOCATION_ID);
                            isRecordingLocation = false;
                        }

                    }

                }
            });

        }
    }

    @Override
    public void onDestroy() {
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        mNotifyMgr.cancel(NotificationActivity.LOCATION_ID);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onLocationChanged(Location location) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        boolean enabledTracking = sharedPreferences.getBoolean(GeneralKeys.KEY_SMAP_USER_LOCATION, false);

        Timber.i("%%%%%%%%%%%%%%%%%%%% location changed: ");

        if(isValidLocation(location) && isAccurateLocation(location)) {
            Collect.getInstance().setLocation(location);

            // Save the location in the database
            if (enabledTracking) {
                Timber.i("+++++++++++++++++++++++++++++ tracking");
                //long newTime = System.currentTimeMillis();
                if(lastLocation == null || location.distanceTo(lastLocation) > Constants.GPS_DISTANCE) {
                    Timber.i("^^^^^^^^^^^^^^^^^^^^^^^^^^ insert point");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("locationChanged"));  // update map
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


}
