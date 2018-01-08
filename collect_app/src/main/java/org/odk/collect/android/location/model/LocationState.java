package org.odk.collect.android.location.model;

import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

public class LocationState {

    @Nullable
    private final LatLng currentLocation;

    @Nullable
    private final LatLng markedLocation;

    public LocationState(@Nullable Location currentLocation,
                         @Nullable LatLng markedLocation) {
        this.currentLocation = currentLocation != null
                ? new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())
                : null;

        this.markedLocation = markedLocation;
    }

    @Nullable
    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    @Nullable
    public LatLng getMarkedLocation() {
        return markedLocation;
    }
}

