package org.odk.collect.android.location.client;

import android.location.Location;
import android.support.annotation.Nullable;

class TestLocationListener implements com.google.android.gms.location.LocationListener {

    @Nullable
    private Location lastLocation;

    @Nullable
    Location getLastLocation() {
        return lastLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLocation = location;
    }
}