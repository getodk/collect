/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.logic.actions.setlocation;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.location.LocationListener;

import org.javarosa.core.model.actions.setlocation.SetLocationAction;
import org.javarosa.core.model.instance.TreeReference;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.location.client.GoogleLocationClient;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.utilities.GeoUtils;

import timber.log.Timber;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_BACKGROUND_LOCATION;

/**
 * An Android-specific implementation of {@link SetLocationAction}. When the action is triggered,
 * the first location available is saved and the highest-accuracy reading within
 * {@link #SECONDS_TO_CONSIDER_UPDATES} seconds will replace it. If no location is available within
 * {@link #SECONDS_TO_CONSIDER_UPDATES} seconds, no location is written.
 *
 * It is possible for the same action to be triggered multiple times within
 * {@link #SECONDS_TO_CONSIDER_UPDATES} seconds (if the action is triggered by a value changed
 * event, for example). In that case, the highest accuracy reading resets for subsequent triggers of
 * the action.
 */
public class CollectSetLocationAction extends SetLocationAction implements LocationListener, LocationClient.LocationClientListener {
    private static final int SECONDS_TO_CONSIDER_UPDATES = 20;

    private LocationClient locationClient;
    private Location highestAccuracyReading;

    private Handler timerHandler;

    public CollectSetLocationAction() {
        // For serialization
    }

    // Needed to set the action name.
    CollectSetLocationAction(TreeReference targetReference) {
        super(targetReference);
    }

    @Override
    public void requestLocationUpdates() {
        // The action has been triggered again so make sure to consider the first reading. This is
        // especially important in the case of repeats when the action may be triggered over and
        // over again.
        highestAccuracyReading = null;

        // Only start acquiring location if the Collect preference allows it. If it's not allowed,
        // leave the target field blank.
        if (GeneralSharedPreferences.getInstance().getBoolean(KEY_BACKGROUND_LOCATION, true)) {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (locationClient == null) {
                    locationClient = new GoogleLocationClient(Collect.getInstance().getApplicationContext());
                    locationClient.setPriority(LocationClient.Priority.PRIORITY_BALANCED_POWER_ACCURACY);
                    locationClient.setListener(this);
                    locationClient.start();
                }
            });

            if (timerHandler == null) {
                timerHandler = new Handler(Looper.getMainLooper());
            }

            // If this action is triggered more than once, only the most recently-triggered instance
            // actually sets its target. For example, in a repeat, if a second repeat is added
            // within SECONDS_TO_CONSIDER_UPDATES of the first repeat, the first repeat will stop
            // being updated at that moment but the window of time during which the client is active
            // is extended for the second repeat.
            timerHandler.removeCallbacksAndMessages(null);
            timerHandler.postDelayed(() -> {
                if (locationClient != null) {
                    locationClient.stop();
                    Timber.i("Setlocation action for " + getContextualizedTargetReference() + ": stopping location updates");
                }
            }, SECONDS_TO_CONSIDER_UPDATES * 1000);
        }
    }

    //region LocationListener
    /**
     * When the location updates, if location updates are allowed, no location has been received yet
     * or the new location has a higher accuracy than previous locations, save the location in the
     * model, matching the target node's type.
     *
     * If the background location preference is toggled to disabled while location is being acquired,
     * the node receiving the location will be cleared.
     */
    @Override
    public void onLocationChanged(Location location) {
        if (GeneralSharedPreferences.getInstance().getBoolean(KEY_BACKGROUND_LOCATION, true)) {
            Timber.i("Setlocation action for " + getContextualizedTargetReference() + ": location update");

            if (highestAccuracyReading == null || !location.hasAccuracy()
                    || location.hasAccuracy() && highestAccuracyReading.hasAccuracy() && location.getAccuracy() > highestAccuracyReading.getAccuracy()) {
                highestAccuracyReading = location;

                String formattedLocation = GeoUtils.formatLocationResultString(location);

                saveLocationValue(formattedLocation);
            }
        } else {
            saveLocationValue("");
        }
    }
    //endregion

    //region LocationClientListener

    /**
     * Request location updates as soon as possible.
     */
    @Override
    public void onClientStart() {
        Timber.i("Setlocation action for " + getTargetReference() + ": starting location updates");
        try {
            locationClient.requestLocationUpdates(this);
        } catch (SecurityException e) {
            // Device-level location permissions have not been granted. The user will be prompted to
            // provide permissions. It will be too late for this triggered action but will work for
            // future ones.
        }
    }

    @Override
    public void onClientStartFailure() {

    }

    @Override
    public void onClientStop() {

    }
}
