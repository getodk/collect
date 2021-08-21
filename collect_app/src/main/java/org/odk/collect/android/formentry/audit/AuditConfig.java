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

package org.odk.collect.android.formentry.audit;

import org.odk.collect.location.LocationClient;

import java.util.Locale;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**
 * This class is responsible for storing the current audit configuration, which contains three
 * parameters: locationPriority, locationMinInterval and locationMaxAge.
 */
public class AuditConfig {

    private static final long MIN_ALLOWED_LOCATION_MIN_INTERVAL = 1000;

    /**
     * The locationPriority of location requests
     */
    private final LocationClient.Priority locationPriority;

    /**
     * The desired minimum interval in milliseconds that the location will be fetched
     */
    private final Long locationMinInterval;

    /**
     * The time in milliseconds that location will be valid
     */
    private final Long locationMaxAge;

    /**
     * True if new answers should be added in the audit file
     */
    private final boolean isTrackingChangesEnabled;

    private final boolean isIdentifyUserEnabled;
    private final boolean isTrackChangesReasonEnabled;

    public AuditConfig(String mode, String locationMinInterval, String locationMaxAge, boolean isTrackingChangesEnabled, boolean isIdentifyUserEnabled, boolean isTrackChangesReasonEnabled) {
        this.locationPriority = mode != null ? getMode(mode) : null;
        this.locationMinInterval = locationMinInterval != null ? Long.parseLong(locationMinInterval) * 1000 : null;
        this.locationMaxAge = locationMaxAge != null ? Long.parseLong(locationMaxAge) * 1000 : null;
        this.isTrackingChangesEnabled = isTrackingChangesEnabled;
        this.isIdentifyUserEnabled = isIdentifyUserEnabled;
        this.isTrackChangesReasonEnabled = isTrackChangesReasonEnabled;
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
    public Long getLocationMinInterval() {
        return locationMinInterval == null
                ? null
                : locationMinInterval > MIN_ALLOWED_LOCATION_MIN_INTERVAL
                    ? locationMinInterval
                    : MIN_ALLOWED_LOCATION_MIN_INTERVAL;
    }

    @Nullable
    public Long getLocationMaxAge() {
        return locationMaxAge;
    }

    public boolean isLocationEnabled() {
        return locationPriority != null && locationMinInterval != null && locationMaxAge != null;
    }

    public boolean isTrackingChangesEnabled() {
        return isTrackingChangesEnabled;
    }

    public boolean isIdentifyUserEnabled() {
        return isIdentifyUserEnabled;
    }

    public boolean isTrackChangesReasonEnabled() {
        return isTrackChangesReasonEnabled;
    }

    public static class Builder {
        private String mode;
        private String locationMinInterval;
        private String locationMaxAge;
        private boolean isTrackingChangesEnabled;
        private boolean isIdentifyUserEnabled;
        private boolean isTrackChangesReasonEnabled;

        public Builder setMode(String mode) {
            this.mode = mode;
            return this;
        }

        public Builder setLocationMinInterval(String locationMinInterval) {
            this.locationMinInterval = locationMinInterval;
            return this;
        }

        public Builder setLocationMaxAge(String locationMaxAge) {
            this.locationMaxAge = locationMaxAge;
            return this;
        }

        public Builder setIsTrackingChangesEnabled(boolean isTrackingChangesEnabled) {
            this.isTrackingChangesEnabled = isTrackingChangesEnabled;
            return this;
        }

        public Builder setIsIdentifyUserEnabled(boolean isIdentifyUserEnabled) {
            this.isIdentifyUserEnabled = isIdentifyUserEnabled;
            return this;
        }

        public Builder setIsTrackChangesReasonEnabled(boolean isTrackChangesReasonEnabled) {
            this.isTrackChangesReasonEnabled = isTrackChangesReasonEnabled;
            return this;
        }

        public AuditConfig createAuditConfig() {
            return new AuditConfig(mode, locationMinInterval, locationMaxAge, isTrackingChangesEnabled, isIdentifyUserEnabled, isTrackChangesReasonEnabled);
        }
    }
}
