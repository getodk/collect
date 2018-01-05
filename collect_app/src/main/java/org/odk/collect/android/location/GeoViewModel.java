package org.odk.collect.android.location;

import android.graphics.Path;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.architecture.rx.RxMVVMViewModel;
import org.odk.collect.android.injection.config.scopes.PerViewModel;
import org.odk.collect.android.location.usecases.CurrentLocation;
import org.odk.collect.android.location.usecases.IsDraggable;
import org.odk.collect.android.location.usecases.IsReadOnly;
import org.odk.collect.android.location.usecases.MarkedLocation;
import org.odk.collect.android.location.usecases.WatchLocation;

import javax.annotation.Nullable;
import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Observable;


@PerViewModel
public class GeoViewModel extends RxMVVMViewModel {

    private final BehaviorRelay<Boolean> isPauseVisible = BehaviorRelay.createDefault(false);
    private final BehaviorRelay<Boolean> isReloadEnabled = BehaviorRelay.createDefault(false);
    private final BehaviorRelay<Boolean> isShowEnabled = BehaviorRelay.createDefault(false);

    private final BehaviorRelay<Optional<MarkerOptions>> markerOptions =
            BehaviorRelay.createDefault(Optional.absent());

    private final PublishRelay<LatLng> shouldZoomToLatLng = PublishRelay.create();
    private final PublishRelay<Object> shouldShowGpsDisabledAlert = PublishRelay.create();

    @NonNull
    private final IsDraggable isDraggable;

    @NonNull
    private final IsReadOnly isReadOnly;

    @NonNull
    private final WatchLocation watchLocation;

    @NonNull
    private final CurrentLocation currentLocation;

    @NonNull
    private final MarkedLocation markedLocation;

    @Inject
    GeoViewModel(@NonNull IsDraggable isDraggable,
                 @NonNull IsReadOnly isReadOnly,
                 @NonNull WatchLocation watchLocation,
                 @NonNull CurrentLocation currentLocation,
                 @NonNull MarkedLocation markedLocation) {

        this.isDraggable = isDraggable;
        this.isReadOnly = isReadOnly;
        this.watchLocation = watchLocation;
        this.currentLocation = currentLocation;
        this.markedLocation = markedLocation;
    }

    @Override
    protected void onCreate(@Nullable Bundle parameters) {
        super.onCreate(parameters);

        watchLocation.observeAvailability()
                .compose(bindToLifecycle())
                .filter(isAvailable -> !isAvailable)
                .subscribe(shouldShowGpsDisabledAlert);

        currentLocation.observe()
                .compose(bindToLifecycle())
                .map(location -> new LatLng(location.getLatitude(), location.getLongitude()))
                .subscribe(latLng -> {

                    isShowEnabled.accept(true);
                    isReloadEnabled.accept(true);

                    markedLocation.update(latLng);
                });

        currentLocation.observe()
                .withLatestFrom(markedLocation.observe(), (__, latLngOptional) -> !latLngOptional.isPresent())
                .subscribe(isReloadEnabled);

        markedLocation.observe()
                .map(latLngOptional -> {
                    // Compiler was having trouble with the types unless I used this structure:
                    MarkerOptions options = null;
                    if (latLngOptional.isPresent()) {
                        options= new MarkerOptions();
                        options.position(latLngOptional.get());
                    }

                    return Optional.fromNullable(options);

                }).subscribe(markerOptions);


    }

    Observable<Boolean> isLocationStatusVisible() {
        return Observable.just(false);
    }

    Observable<Boolean> isLocationInfoVisible() {
        return Observable.just(false);
    }

    Observable<Boolean> isShowLocationEnabled() {
        return Observable.just(false);
    }

    Observable<LatLng> shouldZoomToLatLng() {
        return shouldZoomToLatLng.hide();
    }

    Observable<Boolean> isPauseVisible() {
        return isPauseVisible.hide();
    }

    Observable<Boolean> isReloadEnabled() {
        return isReloadEnabled.hide();
    }

    Observable<Boolean> isShowEnabled() {
        return isShowEnabled.hide();
    }

    Observable<Optional<MarkerOptions>> observeMarkerOptions() {
        return markerOptions.hide();
    }

    Observable<Boolean> isReadOnly() {
        return isReadOnly.observe();
    }

    Observable<Boolean> isDraggable() {
        return isDraggable.observe();
    }

    void addLocation() {
        // Clear the marker.
    }

    void pause() {

    }

    void showLocation() {

    }

    void showLayers() {

    }

    void clearLocation() {

    }

    void setMarkedLocation(@NonNull LatLng latLng) {
        markedLocation.update(latLng);
    }

    void startWatchingLocation() {
        watchLocation.startWatching();
    }

    void stopWatchingLocation() {
        watchLocation.stopWatching();
    }

    Maybe<String> saveLocation() {
        return null;
    }
}
