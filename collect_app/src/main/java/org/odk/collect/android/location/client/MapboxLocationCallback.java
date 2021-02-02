package org.odk.collect.android.location.client;

import android.location.Location;

import androidx.annotation.NonNull;

import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;

import org.odk.collect.android.geo.MapboxMapFragment;

import java.lang.ref.WeakReference;

// https://docs.mapbox.com/android/core/guides/#requesting-location-updates
// Replace mock location accuracy with 0 as in LocationClient implementations since Mapbox uses its own location engine.
public class MapboxLocationCallback implements LocationEngineCallback<LocationEngineResult> {

    private final WeakReference<MapboxMapFragment> mapRef;

    public MapboxLocationCallback(MapboxMapFragment map) {
        mapRef = new WeakReference<>(map);
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        MapboxMapFragment map = mapRef.get();
        Location location = result.getLastLocation();
        if (map != null && location != null) {
            if (location.isFromMockProvider() || location.getAccuracy() < 0) {
                location.setAccuracy(0);
            }
            map.onLocationChanged(location);
        }
    }

    @Override
    public void onFailure(@NonNull Exception exception) { }
}
