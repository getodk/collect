package org.odk.collect.android.location;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.architecture.rx.RxMVVMViewModel;
import org.odk.collect.android.injection.config.scopes.PerViewModel;
import org.odk.collect.android.location.usecases.CurrentLocation;
import org.odk.collect.android.location.usecases.InfoText;
import org.odk.collect.android.location.usecases.InitialLocation;
import org.odk.collect.android.location.usecases.IsDraggable;
import org.odk.collect.android.location.usecases.IsReadOnly;
import org.odk.collect.android.location.usecases.MarkedLocation;
import org.odk.collect.android.location.usecases.StatusText;
import org.odk.collect.android.location.usecases.WatchLocation;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Observable;


@PerViewModel
public class GeoViewModel extends RxMVVMViewModel {

    // Inputs:
    @NonNull
    private final IsDraggable isDraggable;

    @NonNull
    private final IsReadOnly isReadOnly;

    @NonNull
    private final InitialLocation initialLocation;

    @NonNull
    private final WatchLocation watchLocation;

    @NonNull
    private final CurrentLocation currentLocation;

    @NonNull
    private final MarkedLocation markedLocation;

    @NonNull
    private final InfoText infoText;

    @NonNull
    private final StatusText statusText;

    // Outputs:

    @NonNull
    private final BehaviorRelay<Optional<MarkerOptions>> markerOptions =
            BehaviorRelay.createDefault(Optional.absent());

    @Inject
    GeoViewModel(@NonNull IsDraggable isDraggable,
                 @NonNull IsReadOnly isReadOnly,
                 @NonNull InitialLocation initialLocation,
                 @NonNull WatchLocation watchLocation,
                 @NonNull CurrentLocation currentLocation,
                 @NonNull MarkedLocation markedLocation, @NonNull InfoText infoText, @NonNull StatusText statusText) {

        this.isDraggable = isDraggable;
        this.isReadOnly = isReadOnly;
        this.initialLocation = initialLocation;
        this.watchLocation = watchLocation;
        this.currentLocation = currentLocation;
        this.markedLocation = markedLocation;
        this.infoText = infoText;
        this.statusText = statusText;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
    }

    Observable<Boolean> hasInitialLocation() {
        return initialLocation.observe()
                .map(Optional::isPresent);
    }

    Observable<String> infoText() {
        return infoText.observe();
    }

    Observable<String> statusText() {
        return statusText.observe();
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

    Observable<Optional<MarkerOptions>> observeMarkerOptions() {
        return markerOptions.hide();
    }

    Observable<Boolean> isReadOnly() {
        return isReadOnly.observe();
    }

    Observable<Boolean> isDraggable() {
        return isDraggable.observe();
    }

    Observable<Object> shouldShowGpsAlert() {
        return watchLocation.observeAvailability()
                .filter(isAvailable -> !isAvailable)
                .map(__ -> this);
    }

    void addLocation() {

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
