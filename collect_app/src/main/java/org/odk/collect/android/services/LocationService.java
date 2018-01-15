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
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.TraceUtilities;
import org.odk.collect.android.location.LocationClient;
import org.odk.collect.android.location.LocationClients;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.tasks.DownloadTasksTask;
import org.odk.collect.android.utilities.Constants;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

/**
 * Created by neilpenman on 2018-01-11.
 */

/*
 * Respond to a notification from the server
 */
public class LocationService extends Service implements LocationListener, LocationClient.LocationClientListener {

    Handler mHandler = new Handler();       // Background thread to check for enabling / disabling the location listener
    private LocationClient locationClient;
    private boolean isRecordingLocation = false;
    private Timer mTimer;
    private LocationService mLocationService = null;
    String TAG = "Location Service";


    public LocationService(Context applicationContext) {
        super();
    }

    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "======================= Start Location Service");


        if (mTimer == null) {
            mTimer = new Timer();
        }
        mLocationService = this;
        mTimer.scheduleAtFixedRate(new CheckEnabledTimerTask(), 0, 10000);

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
                    boolean enabled = sharedPreferences.getBoolean(PreferenceKeys.KEY_SMAP_USER_LOCATION, false);

                    if (enabled == isRecordingLocation) {
                        // No change
                    } else {

                        NotificationManager mNotifyMgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                        if (enabled) {
                            Log.i(TAG, "=================== Location Recording turned on");
                            if(locationClient != null) {
                                locationClient.stop();
                            }
                            locationClient = LocationClients.clientForContext(getApplicationContext());
                            locationClient.setListener(mLocationService);
                            locationClient.start();

                        /*
                         * Notify the user
                         */
                            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.ic_stat_crosshairs)
                                            .setLargeIcon(BitmapFactory.decodeResource(Collect.getInstance().getBaseContext().getResources(),
                                                    R.drawable.ic_stat_crosshairs))
                                            .setSound(uri)
                                            .setContentTitle(getString(R.string.app_name))
                                            .setContentText(getString(R.string.smap_location_tracking));
                            mNotifyMgr.notify(NotificationActivity.LOCATION_ID, mBuilder.build());

                        } else {
                            Log.i(TAG, "=================== Location Recording turned off");
                            locationClient.stop();
                            mNotifyMgr.cancel(NotificationActivity.LOCATION_ID);
                        }
                        isRecordingLocation = enabled;
                    }

                }
            });

        }
    }

    @Override
    public void onDestroy() {
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(locationClient != null) {
            locationClient.stop();
        }
        mNotifyMgr.cancel(NotificationActivity.LOCATION_ID);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onClientStart() {
        locationClient.requestLocationUpdates(this);
    }

    @Override
    public void onClientStartFailure() {

    }

    @Override
    public void onClientStop() {

    }

    @Override
    public void onLocationChanged(Location location) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
        boolean enabledTracking = sharedPreferences.getBoolean(PreferenceKeys.KEY_SMAP_USER_LOCATION, false);
        boolean enabledGPS = false;

        if(isValidLocation(location) && isAccurateLocation(location)) {
            Collect.getInstance().setLocation(location);

            // Notify any activity interested that there is a new location
            if (enabledGPS || enabledTracking) {
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("locationChanged"));
            }

            // Save the location in the database
            if (enabledTracking) {
                TraceUtilities.insertPoint(location);
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
            Log.d(TAG, "Ignore location. Poor accuracy.");
            accurate = false;
        }
        return accurate;
    }


}
