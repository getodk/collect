package org.odk.collect.android.location.model;

import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

public class ZoomData {

    @Nullable
    private final LatLng currentLocation;

    @Nullable
    private final LatLng markedLocation;

    public ZoomData(@Nullable Location currentLocation,
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

    public boolean isEmpty() {
        return currentLocation == null && markedLocation == null;
    }
}

