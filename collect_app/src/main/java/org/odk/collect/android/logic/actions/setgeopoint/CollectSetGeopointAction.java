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

package org.odk.collect.android.logic.actions.setgeopoint;

import android.location.Location;

import com.google.android.gms.location.LocationListener;

import org.javarosa.core.model.actions.setgeopoint.SetGeopointAction;
import org.javarosa.core.model.instance.TreeReference;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.location.GoogleFusedLocationClient;
import org.odk.collect.android.location.client.MaxAccuracyWithinTimeoutLocationClientWrapper;
import org.odk.collect.geo.GeoUtils;
import org.odk.collect.android.utilities.PlayServicesChecker;

import timber.log.Timber;

import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_BACKGROUND_LOCATION;

/**
 * An Android-specific implementation of {@link SetGeopointAction}.
 *
 * When the action is triggered, the first location available is saved and the highest-accuracy
 * reading within {@link #SECONDS_TO_CONSIDER_UPDATES} seconds will replace it. If no location is
 * available within {@link #SECONDS_TO_CONSIDER_UPDATES} seconds, no location is written.
 *
 * It is possible for the same action to be triggered multiple times within
 * {@link #SECONDS_TO_CONSIDER_UPDATES} seconds if the action is triggered by a value changed
 * event, for example. In that case, the highest accuracy reading resets each time the action is
 * triggered.
 *
 * In a repeat, the target node for subsequent times the action is triggered could be different than
 * the target node the first time it was triggered. In that case, only the current target node is
 * updated.
 */
public class CollectSetGeopointAction extends SetGeopointAction implements LocationListener {
    private static final int SECONDS_TO_CONSIDER_UPDATES = 20;

    private MaxAccuracyWithinTimeoutLocationClientWrapper maxAccuracyLocationClient;

    public CollectSetGeopointAction() {
        // For serialization
    }

    // Needed to set the action name.
    CollectSetGeopointAction(TreeReference targetReference) {
        super(targetReference);
    }

    @Override
    public void requestLocationUpdates() {
        // Do initialization on first location request so the client doesn't need to be serialized
        if (maxAccuracyLocationClient == null) {
            maxAccuracyLocationClient = new MaxAccuracyWithinTimeoutLocationClientWrapper(new GoogleFusedLocationClient(Collect.getInstance()), this);
        }

        // Only start acquiring location if the Collect preference allows it and Google Play
        // Services are available. If it's not allowed, leave the target field blank.
        if (isBackgroundLocationEnabled()
            && new PlayServicesChecker().isGooglePlayServicesAvailable(Collect.getInstance().getApplicationContext())) {
            maxAccuracyLocationClient.requestLocationUpdates(SECONDS_TO_CONSIDER_UPDATES);
        }
    }

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
        if (isBackgroundLocationEnabled()) {
            Timber.i("Setgeopoint action for " + getContextualizedTargetReference() + ": location update");

            String formattedLocation = GeoUtils.formatLocationResultString(location);
            saveLocationValue(formattedLocation);
        } else {
            saveLocationValue("");
        }
    }

    private boolean isBackgroundLocationEnabled() {
        return DaggerUtils
                .getComponent(Collect.getInstance())
                .settingsProvider()
                .getUnprotectedSettings()
                .getBoolean(KEY_BACKGROUND_LOCATION);
    }
}
