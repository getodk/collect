/*
 * Copyright 2018 Nafundi
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

package org.odk.collect.android.logic;

import org.odk.collect.android.location.client.LocationClient;

import java.util.Locale;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

public class Audit {

    /**
     * The locationPriority of location requests
     */
    private final LocationClient.Priority locationPriority;

    /**
     * The desired minimum interval in seconds that the location will be fetched
     */
    private final Integer locationInterval;

    /**
     * The time in seconds that location will be valid
     */
    private final Integer locationAge;

    Audit(String mode, String locationInterval, String locationAge) {
        this.locationPriority = mode != null ? getMode(mode) : null;
        this.locationInterval = locationInterval != null ? Integer.parseInt(locationInterval) : null;
        this.locationAge = locationAge != null ? Integer.parseInt(locationAge) : null;
    }

    private LocationClient.Priority getMode(@NonNull String mode) {
        switch (mode.toLowerCase(Locale.US)) {
            case "balanced":
                return LocationClient.Priority.PRIORITY_BALANCED_POWER_ACCURACY;
            case "low_power":
            case "low-power":
                return LocationClient.Priority.PRIORITY_LOW_POWER;
            case "no_power":
            case "no-power":
                return LocationClient.Priority.PRIORITY_NO_POWER;
            default:
                return LocationClient.Priority.PRIORITY_HIGH_ACCURACY;
        }
    }

    @Nullable
    public LocationClient.Priority getLocationPriority() {
        return locationPriority;
    }

    @Nullable
    public Integer getLocationInterval() {
        return locationInterval;
    }

    @Nullable
    public Integer getLocationAge() {
        return locationAge;
    }

    public boolean collectLocationCoordinates() {
        return locationPriority != null && locationInterval != null && locationAge != null;
    }
}
