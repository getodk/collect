package org.odk.collect.android.location;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtility {

    @NonNull
    public static Location randomLocation() {
        Location location = mock(Location.class);

        when(location.getLatitude()).thenReturn(Math.random());
        when(location.getLongitude()).thenReturn(Math.random());

        return location;
    }

    @NonNull
    public static LatLng randomLatLng() {
        return new LatLng(Math.random(), Math.random());
    }

    @NonNull
    public static LatLng locationToLatLng(@NonNull Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
