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

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZoomData zoomData = (ZoomData) o;

        if (currentLocation != null ? !currentLocation.equals(zoomData.currentLocation) : zoomData.currentLocation != null)
            return false;

        return markedLocation != null ? markedLocation.equals(zoomData.markedLocation) : zoomData.markedLocation == null;
    }

    @Override
    public int hashCode() {
        int result = currentLocation != null ? currentLocation.hashCode() : 0;
        result = 31 * result + (markedLocation != null ? markedLocation.hashCode() : 0);
        return result;
    }
}

