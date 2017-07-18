package org.odk.collect.android.receivers;

/*
 * Copyright (C) 2014 Smap Consulting Pty Ltd
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
 *
 * Modified version of odkCollect NetworkReceiver class
 */


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.Constants;
import org.odk.collect.android.utilities.TraceUtilities;


/**
 * This Receiver class is used to listen for Broadcast Intents that announce
 * that a location change has occurred. This is used instead of a LocationListener
 * within an Activity is our only action is to start a service.
 */
public class LocationChangedReceiver extends BroadcastReceiver {

    protected static String TAG = "LocationChangedReceiver";
    private SharedPreferences settings = null;

    /**
     * When a new location is received, extract it from the Intent and use
     * TODO start a service to handle new location
     *
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String locationKey = LocationManager.KEY_LOCATION_CHANGED;
        String providerEnabledKey = LocationManager.KEY_PROVIDER_ENABLED;
        if (intent.hasExtra(providerEnabledKey)) {
            if (!intent.getBooleanExtra(providerEnabledKey, true)) {
                Log.i(TAG, "===================== Provider disabled");
                if (!intent.getBooleanExtra(providerEnabledKey, true)) {
                    Intent providerDisabledIntent = new Intent("org.odk.collect.android.active_location_update_provider_disabled");
                    context.sendBroadcast(providerDisabledIntent);
                }
            }
        }
        if (intent.hasExtra(locationKey)) {
            Location location = (Location)intent.getExtras().get(locationKey);

            if(isValidLocation(location) && isAccurateLocation(location)) {
                Log.d(TAG, "============== Updating location");

                // Save the current location
                Collect.getInstance().setLocation(location);

                // Notify any activity interested that there is a new location
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("locationChanged"));

                // Save the location in the database
                if (settings == null) {
                    settings = PreferenceManager.getDefaultSharedPreferences(context);
                }
                if (settings.getBoolean(PreferencesActivity.KEY_STORE_USER_TRAIL, false)) {
                    TraceUtilities.insertPoint(location);
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
            Log.d(TAG, "Ignore onLocationChangedAsync. Poor accuracy.");
            accurate = false;
        }
        return accurate;
    }
}