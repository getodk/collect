package org.odk.collect.android.location.domain;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.odk.collect.android.injection.scopes.ActivityScope;

import javax.inject.Inject;

@ActivityScope
public class MarkerDragListener implements GoogleMap.OnMarkerDragListener {

    @Inject
    public MarkerDragListener() {
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng latLng = marker.getPosition();
        boolean isDragged = true;
        boolean captureLocation = true;
        boolean setClear = false;

        // TODO: Setup listener.
    }
}
