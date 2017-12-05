package org.odk.collect.android.location.usecases;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * @author James Knight
 */

public class OnMarkerDrag implements GoogleMap.OnMarkerDragListener {
    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
//        latLng = marker.getPosition();
//        isDragged = true;
//        captureLocation = true;
//        setClear = false;
//        map.animateCamera(
//                CameraUpdateFactory.newLatLngZoom(latLng, map.getCameraPosition().zoom));

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}
