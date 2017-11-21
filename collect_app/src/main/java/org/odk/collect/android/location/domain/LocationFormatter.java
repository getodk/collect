package org.odk.collect.android.location.domain;


import android.location.Location;

import org.odk.collect.android.injection.scopes.ActivityScope;

import javax.inject.Inject;

@ActivityScope
public class LocationFormatter {

    @Inject
    public LocationFormatter() {

    }

    public String formatForLocation(Location location) {
        return formatForData(location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy());
    }

    public String formatForData(double latitude, double longitude, double altitude, float accuracy) {
        return String.format("%s %s %s %s", latitude, longitude, altitude, accuracy);
    }
}
