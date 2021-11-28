package org.odk.collect.android.location.client;

import android.location.Location;

import androidx.annotation.NonNull;

import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;

import org.odk.collect.android.geo.MapboxMapFragment;
import org.odk.collect.location.LocationUtils;

import java.lang.ref.WeakReference;

// https://docs.mapbox.com/android/core/guides/#requesting-location-updates
// Replace mock location accuracy with 0 as in LocationClient implementations since Mapbox uses its own location engine.
public class MapboxLocationCallback implements LocationEngineCallback<LocationEngineResult> {

    private final WeakReference<MapboxMapFragment> mapRef;
    private boolean retainMockAccuracy;

    public MapboxLocationCallback(MapboxMapFragment map) {
        mapRef = new WeakReference<>(map);
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        MapboxMapFragment map = mapRef.get();
        Location location = result.getLastLocation();
        if (map != null && location != null) {
            map.onLocationChanged(LocationUtils.sanitizeAccuracy(location, retainMockAccuracy));
        }
    }

    @Override
    public void onFailure(@NonNull Exception exception) { }

    public void setRetainMockAccuracy(boolean retainMockAccuracy) {
        this.retainMockAccuracy = retainMockAccuracy;
    }
}
