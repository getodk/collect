package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

@PerViewModel
public class ClearLocation {

    @NonNull
    private final CurrentLocation currentLocation;

    private Marker marker = null;
    private LatLng latLng = null;

    private boolean setClear = false;
    private boolean isDragged = false;

    private boolean captureLocation = false;
    private boolean draggable = false;
    private boolean intentDraggable = false;

    private boolean locationFromIntent = false;
    private boolean isReadOnly = false;


    @Inject
    public ClearLocation(@NonNull CurrentLocation currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void clear(GoogleMap googleMap) {
        if (marker != null) {
            marker.remove();
        }

        currentLocation.clear();

        // reloadLocation.setEnabled(true);
        latLng = null;
        marker = null;
        setClear = true;
        isDragged = false;
        captureLocation = false;
        draggable = intentDraggable;
        locationFromIntent = false;

        if (draggable && !isReadOnly) {
            googleMap.setOnMarkerDragListener(null);
            googleMap.setOnMapLongClickListener(null);

            if (marker != null) {
                marker.setDraggable(true);
            }
        }
    }
}
