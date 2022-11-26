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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.receivers.LocationReceiver;
import org.odk.collect.android.utilities.Constants;

import java.util.Timer;
import java.util.TimerTask;

import androidx.core.app.NotificationCompat;
import timber.log.Timber;

/**
 * Created by neilpenman on 2018-01-11.
 */

/*
 * Get locations
 */
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks {

    Handler mHandler = new Handler();           // Background thread to check for enabling / disabling the location listener
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isRecordingLocation = false;
    private Timer mTimer;
    String FG_CHANNEL_ID = "smap_foreground_channel";

    public LocationService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        Timber.i("======================= Start Location Service");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        isRecordingLocation = sharedPreferences.getBoolean(GeneralKeys.KEY_SMAP_ENABLE_GEOFENCE, true);

        if (mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.scheduleAtFixedRate(new CheckEnabledTimerTask(), 0, 60000);  // Peiodically check to see if location tracking is disabled

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        requestLocationUpdates();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel chan = new NotificationChannel(FG_CHANNEL_ID, "Notifications", NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);

            Notification.Builder builder = new Notification.Builder(this, FG_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("some text")
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(1, notification);

        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("some text")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1, notification);
        }

        return START_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Timber.i("++++++++++Connected to provider");
        stopLocationUpdates();
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.i("+++++++++++ Connection Suspended");
        stopLocationUpdates();
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
                    isRecordingLocation = sharedPreferences.getBoolean(GeneralKeys.KEY_SMAP_ENABLE_GEOFENCE, true);

                    // Restart location monitoring - Incase permission was disabled and then re-enabled
                    stopLocationUpdates();
                    requestLocationUpdates();
                }
            });

        }
    }

    /*
     * Methods ot support location broadcast receiver
     */
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(Constants.GPS_INTERVAL / 2);
        locationRequest.setInterval(Constants.GPS_INTERVAL);
    }

    private void requestLocationUpdates() {
        if(isRecordingLocation) {
            try {
                Timber.i("+++++++ Requesting location updates");
                fusedLocationClient.requestLocationUpdates(locationRequest, getPendingIntent());
            } catch (SecurityException e) {
                Timber.i("%%%%%%%%%%%%%%%%%%%% location recording not permitted: ");
            }
        } else {
            Timber.i("+++++++ Location updates disabled");
        }
    }

    private void stopLocationUpdates() {
        Timber.i("=================== Location Recording turned off");
        if(fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(getPendingIntent());
        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationReceiver.class);
        intent.setAction(LocationReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

}
