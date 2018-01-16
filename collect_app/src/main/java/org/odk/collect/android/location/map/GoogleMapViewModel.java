package org.odk.collect.android.location.map;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.utilities.Rx;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * @author James Knight
 */

public class GoogleMapViewModel implements MapViewModel {

    // Internal state:
    @NonNull
    private final BehaviorRelay<GoogleMap> mapRelay =
            BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<Boolean> isDraggableRelay =
            BehaviorRelay.createDefault(true);

    @NonNull
    private final BehaviorRelay<Optional<Marker>> markerRelay =
            BehaviorRelay.createDefault(Optional.absent());

    // Inputs:
    @NonNull
    private final Consumer<Optional<Marker>> updateMarker =
            markerRelay;

    @NonNull
    private final Consumer<Boolean> updateIsDraggable =
            isDraggableRelay;

    // Outputs:
    @NonNull
    private final Observable<GoogleMap> observeMap =
            mapRelay.hide();

    @NonNull
    private final Observable<Boolean> observeDraggable =
            isDraggableRelay.hide();

    @NonNull
    private final Observable<Optional<Marker>> observeMarker =
            markerRelay.hide();

    @NonNull
    @Override
    public Observable<LatLng> observeMarkedLocation() {
        return markerRelay
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Marker::getPosition);
    }

    @NonNull
    @Override
    public Observable<Object> observeClearedLocation() {
        return markerRelay
                .map(Optional::isPresent)
                .filter(Rx::isFalse)
                .skip(1)
                .map(Rx::toEvent);
    }

    @NonNull
    @Override
    public Completable markLocation(@NonNull LatLng latLng) {
        return observeMap.withLatestFrom(observeMarker, (map, markerOptional) -> {
            if (markerOptional.isPresent()) {
                Marker marker = markerOptional.get();
                marker.setPosition(latLng);

                return marker;
            }


            MarkerOptions options = new MarkerOptions()
                    .position(latLng);

            return map.addMarker(options);

        })
                .withLatestFrom(observeDraggable, (marker, isDraggable) -> {
                    marker.setDraggable(isDraggable);
                    return marker;
                })
                .map(Optional::of)
                .flatMapCompletable(markerOptional -> {
                    updateMarker.accept(markerOptional);
                    return Completable.complete();
                });
    }

    @NonNull
    @Override
    public Completable clearMarkedLocation() {
        return observeMarker.firstOrError()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .doOnSuccess(Marker::remove)
                .map(marker -> Optional.<Marker>absent())
                .flatMapCompletable(markerOptional -> {
                    updateMarker.accept(markerOptional);
                    return Completable.complete();
                });
    }

    @NonNull
    @Override
    public Completable zoomToLocation(@NonNull LatLng latLng) {
        return observeMap.firstOrError()
                .flatMapCompletable(googleMap -> {
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f);
                    googleMap.animateCamera(update);

                    return Completable.complete();
                });
    }

    @NonNull
    @Override
    public Completable updateIsDraggable(boolean isDraggable) {
        return Completable.defer(() -> {
            updateIsDraggable(isDraggable);
            return Completable.complete();
        });
    }
}
