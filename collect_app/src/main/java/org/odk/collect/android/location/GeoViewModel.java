package org.odk.collect.android.location;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.architecture.rx.RxMVVMViewModel;
import org.odk.collect.android.location.model.ZoomData;
import org.odk.collect.android.location.usecases.CurrentLocation;
import org.odk.collect.android.location.usecases.InfoText;
import org.odk.collect.android.location.usecases.InitialLocation;
import org.odk.collect.android.location.usecases.InitialState;
import org.odk.collect.android.location.usecases.IsDraggable;
import org.odk.collect.android.location.usecases.IsReadOnly;
import org.odk.collect.android.location.usecases.MarkedLocation;
import org.odk.collect.android.location.usecases.StatusText;
import org.odk.collect.android.location.usecases.WatchLocation;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import timber.log.Timber;


public class GeoViewModel extends RxMVVMViewModel {

    @NonNull
    private final InitialState initialState;

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

    @NonNull
    private final PublishRelay<Object> shouldShowLayers = PublishRelay.create();

    @NonNull
    private final PublishRelay<ZoomData> shouldShowZoomDialog = PublishRelay.create();

    // Outputs:

    @Inject
    GeoViewModel(@NonNull InitialState initialState,
                 @NonNull IsDraggable isDraggable,
                 @NonNull IsReadOnly isReadOnly,
                 @NonNull InitialLocation initialLocation,
                 @NonNull WatchLocation watchLocation,
                 @NonNull CurrentLocation currentLocation,
                 @NonNull MarkedLocation markedLocation,
                 @NonNull InfoText infoText,
                 @NonNull StatusText statusText) {

        this.initialState = initialState;
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
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        initialState.set(bundle);

        // Show Zoom Dialog on first location:
        observeFirstLocation()
                .map(this::locationToLatLng)
                .compose(bindToLifecycle())
                .subscribe(markedLocation::update, Timber::e);

        observeFirstLocation()
                .withLatestFrom(observeZoomData(), Rx::takeRight)
                .compose(bindToLifecycle())
                .subscribe(shouldShowZoomDialog, Timber::e);
    }

    @NonNull
    Observable<String> locationInfoText() {
        return infoText.observe();
    }

    @NonNull
    Observable<String> locationStatusText() {
        return statusText.observe();
    }

    @NonNull
    Observable<Integer> locationInfoVisibility() {
        return hasInitialLocation()
                .map(hasInitialLocation -> hasInitialLocation
                        ? View.GONE
                        : View.VISIBLE)
                .distinctUntilChanged();
    }

    @NonNull
    Observable<Integer> locationStatusVisibility() {
        return hasInitialLocation()
                .map(hasInitialLocation -> hasInitialLocation
                        ? View.GONE
                        : View.VISIBLE)
                .distinctUntilChanged();
    }

    @NonNull
    Observable<Integer> pauseButtonVisibility() {
        return Observable.just(View.GONE);
    }

    @NonNull
    Observable<Boolean> isAddLocationEnabled() {
        return hasCurrentLocation();
    }

    @NonNull
    Observable<Optional<LatLng>> observeMarkedLocation() {
        return markedLocation.observe();
    }

    @NonNull
    Observable<Boolean> isShowLocationEnabled() {
        return Observable.combineLatest(
                hasCurrentLocation(),
                hasMarkedLocation(),
                Rx::or

        ).distinctUntilChanged();
    }

    @NonNull
    Observable<Object> shouldShowGpsAlert() {
        return watchLocation.observeAvailability()
                .filter(isAvailable -> !isAvailable)
                .map(__ -> this);
    }

    @NonNull
    Observable<ZoomData> shouldShowZoomDialog() {
        return shouldShowZoomDialog.hide();
    }

    @NonNull
    Observable<Object> shouldShowLayers() {
        return shouldShowLayers.hide();
    }

    void addLocation() {

    }

    void pause() {

    }

    void showLocation() {
        observeZoomData()
                .compose(bindToLifecycle())
                .subscribe(shouldShowZoomDialog, Timber::e);
    }

    void showLayers() {
        shouldShowLayers.accept(this);
    }

    void clearLocation() {

    }

    void startWatchingLocation() {
        watchLocation.startWatching();
    }

    void stopWatchingLocation() {
        watchLocation.stopWatching();
    }

    Maybe<String> saveLocation() {
        return Maybe.empty();
    }

    @NonNull
    private Observable<Boolean> hasCurrentLocation() {
        return currentLocation.observe()
                .map(Optional::isPresent)
                .distinctUntilChanged();
    }

    @NonNull
    private Observable<Boolean> hasMarkedLocation() {
        return markedLocation.observe()
                .map(Optional::isPresent)
                .distinctUntilChanged();
    }

    @NonNull
    private Observable<Boolean> hasInitialLocation() {
        return initialLocation.observe()
                .map(Optional::isPresent)
                .distinctUntilChanged();
    }

    @NonNull
    private Observable<ZoomData> observeZoomData() {
        return Observable.combineLatest(currentLocation.observe(), markedLocation.observe(),
                (current, marked) -> new ZoomData(
                        current.orNull(),
                        marked.orNull()
                )
        );
    }

    @NonNull
    private Observable<Object> observeFirstLocationReceived() {
        return hasCurrentLocation()
                .filter(Rx::isTrue)
                .withLatestFrom(hasInitialLocation(), Rx::takeRight) // Check initial location.
                .filter(Rx::isFalse) // Only trigger if we don't have one.
                .cast(Object.class);
    }

    @NonNull
    private Observable<Location> observeFirstLocation() {
        return observeFirstLocationReceived()
                .withLatestFrom(currentLocation.observe(), Rx::takeRight)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    @NonNull
    private LatLng locationToLatLng(@NonNull Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
