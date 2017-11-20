package org.odk.collect.android.location.domain;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


public class MarkerDragListener implements GoogleMap.OnMarkerDragListener {

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
