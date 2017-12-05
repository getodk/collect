package org.odk.collect.android.location.usecases;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Completable;

@PerViewModel
public class ReloadLocation {

    private Marker marker = null;
    private LatLng latLng = null;

    private boolean setClear = false;

    private Location location = null;
    private MarkerOptions markerOptions = null;

    private boolean isDraggable = false;
    private boolean isReadOnly = false;

    private boolean captureLocation = false;
    private boolean isDragged = false;

    @Inject
    public ReloadLocation() {

    }

    public Completable reload(@NonNull GoogleMap googleMap) {

        if (marker != null) {
            marker.remove();
        }

        latLng = null;
        marker = null;
        setClear = false;
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        markerOptions.position(latLng);

        if (marker == null) {
            marker = googleMap.addMarker(markerOptions);
            if (isDraggable && !isReadOnly) {
                marker.setDraggable(true);
            }
        }

        captureLocation = true;
        isDragged = false;

        if (latLng != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16));
        }

        return Completable.complete();
    }

}
