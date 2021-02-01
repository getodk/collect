package org.odk.collect.android.location.client;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.LocationListener;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;

public class MapboxLocationCallback implements LocationEngineCallback<LocationEngineResult> {
    @Nullable
    private final LocationListener locationListener;

    public MapboxLocationCallback(@Nullable LocationListener locationListener) {
        this.locationListener = locationListener;
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        Location location = result.getLastLocation();
        if (locationListener != null && location != null) {
            if (location.isFromMockProvider() || location.getAccuracy() < 0) {
                location.setAccuracy(0);
            }
            locationListener.onLocationChanged(location);
        }
    }

    @Override
    public void onFailure(@NonNull Exception exception) { }
}
