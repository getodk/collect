package org.odk.collect.android.location.mapviewmodel;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import io.reactivex.Completable;
import io.reactivex.Observable;

public interface MapViewModel {

    // Outputs:
    @NonNull
    Observable<LatLng> observeMarkedLocation();

    @NonNull
    Observable<Object> observeClearedLocation();

    // Inputs:

    @NonNull
    Completable loadMap();

    @NonNull
    Completable markLocation(@NonNull LatLng latLng);

    @NonNull
    Completable clearMarkedLocation();

    @NonNull
    Completable zoomToLocation(@NonNull LatLng latLng);

    @NonNull
    Completable updateIsDraggable(boolean isDraggable);

    @NonNull
    Completable showLayers();
}
