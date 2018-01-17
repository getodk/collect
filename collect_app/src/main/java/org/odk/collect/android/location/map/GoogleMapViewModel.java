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

import org.odk.collect.android.injection.config.scopes.PerActivity;
import org.odk.collect.android.location.GeoActivity;
import org.odk.collect.android.location.usecases.LoadMap;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * @author James Knight
 */

@PerActivity
public class GoogleMapViewModel implements MapViewModel, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener  {

    @NonNull
    private final GeoActivity geoActivity;

    @NonNull
    private final LoadMap loadMap;

    // Internal state:
    @NonNull
    private final BehaviorRelay<GoogleMap> mapRelay =
            BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<Boolean> isDraggableRelay =
            BehaviorRelay.createDefault(false);

    @NonNull
    private final BehaviorRelay<Optional<Marker>> markerRelay =
            BehaviorRelay.createDefault(Optional.absent());

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

    @Inject
    GoogleMapViewModel(@NonNull GeoActivity geoActivity,
                       @NonNull LoadMap loadMap) {

        this.geoActivity = geoActivity;
        this.loadMap = loadMap;
    }

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
    public Completable loadMap() {
        return loadMap.load()
                .doOnSuccess(googleMap -> {
                    googleMap.setOnMapLongClickListener(this);
                    googleMap.setOnMarkerDragListener(this);

                    mapRelay.accept(googleMap);
                })
                .flatMapCompletable(__ -> Completable.complete());
    }

    @NonNull
    @Override
    public Completable markLocation(@NonNull LatLng latLng) {
        return observeMap.withLatestFrom(observeMarker, observeDraggable, (map, markerOptional, isDraggable) -> {
            Marker marker;
            if (markerOptional.isPresent()) {
                marker = markerOptional.get();
                marker.setPosition(latLng);

            } else {
                MarkerOptions options = new MarkerOptions()
                        .position(latLng);
                marker = map.addMarker(options);
            }

            marker.setDraggable(isDraggable);
            return Optional.of(marker);

        }).flatMapCompletable(markerOptional -> {
            markerRelay.accept(markerOptional);
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
                .map(__ -> Optional.<Marker>absent())
                .flatMapCompletable(markerOptional -> {
                    markerRelay.accept(markerOptional);
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
            isDraggableRelay.accept(isDraggable);
            return Completable.complete();
        });
    }

    @NonNull
    @Override
    public Completable showLayers() {
        return observeMap.flatMapCompletable(googleMap -> {
            new MapHelper(geoActivity, googleMap).showLayersDialog(geoActivity);
            return Completable.complete();
        });
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        markLocation(latLng);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        markerRelay.accept(Optional.of(marker));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        // Do nothing.
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        // Do nothing.
    }
}
