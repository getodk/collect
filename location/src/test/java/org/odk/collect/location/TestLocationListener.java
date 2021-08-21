package org.odk.collect.location;

import android.location.Location;

import androidx.annotation.Nullable;

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
