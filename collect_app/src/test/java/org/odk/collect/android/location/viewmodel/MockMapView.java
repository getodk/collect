package org.odk.collect.android.location.viewmodel;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.location.mapview.MapView;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class MockMapView implements MapView {
    private BehaviorRelay<Optional<LatLng>> markedLocation =
            BehaviorRelay.createDefault(Optional.absent());

    @NonNull
    @Override
    public Observable<LatLng> observeLongPress() {
        return null;
    }

    @NonNull
    @Override
    public Observable<LatLng> observeMarkerMoved() {
        return null;
    }

    @NonNull
    @Override
    public Completable markLocation(@NonNull LatLng latLng) {
        markedLocation.accept(Optional.of(latLng));
        return Completable.complete();
    }

    @NonNull
    @Override
    public Completable zoomToLocation(@NonNull LatLng latLng) {
        return Completable.complete();
    }

    @NonNull
    @Override
    public Completable showLayers() {
        return Completable.complete();
    }
}
