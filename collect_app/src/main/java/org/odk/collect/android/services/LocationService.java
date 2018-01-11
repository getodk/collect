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
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.tasks.DownloadTasksTask;

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

    public LocationService(Context applicationContext) {
        super();
    }

    public LocationService() {
    }

    private boolean isRecordingLocation = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("LocationService", "======================= Start Location Service");

        checkEnabled();


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("LocationService", "======================= Stop Location Service");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    /*
     * Monitor the preferences every 10 seconds to see if location logging is turned off
     * Influenced by: https://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android
     */
    private Timer timer;
    private TimerTask timerTask;
    private void checkEnabled() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0, 10000);
    }

    /*
     * Check preferences every 10 seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("Location Service", "=================== Check Enabled ++++  ");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
                boolean enabled = sharedPreferences.getBoolean(PreferenceKeys.KEY_SMAP_USER_LOCATION, false);

                if(enabled == isRecordingLocation) {
                    Log.i("Location Service", "=================== No Change  " + isRecordingLocation);
                } else if(enabled){
                    Log.i("Location Service", "=================== Recording turned on");
                } else {
                    Log.i("Location Service", "=================== Recording turned off");
                }
                isRecordingLocation = enabled;
            }
        };
    }
}
