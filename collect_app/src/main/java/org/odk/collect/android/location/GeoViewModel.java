package org.odk.collect.android.location;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.architecture.rx.RxMVVMViewModel;
import org.odk.collect.android.location.model.ZoomData;
import org.odk.collect.android.location.usecases.InfoText;
import org.odk.collect.android.location.usecases.InitialState;
import org.odk.collect.android.location.usecases.MarkedLocation;
import org.odk.collect.android.location.usecases.StatusText;
import org.odk.collect.android.location.usecases.WatchPosition;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import timber.log.Timber;


public class GeoViewModel extends RxMVVMViewModel {

    @NonNull
    private final InitialState initialState;

    // Inputs:
    @NonNull
    private final WatchPosition watchPosition;

    @NonNull
    private final MarkedLocation markedLocation;

    @NonNull
    private final InfoText infoText;

    @NonNull
    private final StatusText statusText;

    // Outputs:
    @NonNull
    private final Observable<Integer> locationInfoVisibility;

    @NonNull
    private final Observable<Integer> locationStatusVisibility;

    @NonNull
    private final Observable<Boolean> isShowLocationEnabled;

    @NonNull
    private final Observable<Object> shouldShowGpsAlert;

    @NonNull
    private final Observable<ZoomData> shouldShowZoomDialog;

    @NonNull
    private final Observable<Boolean> hasCurrentPosition;

    @NonNull
    private final Observable<LatLng> onMarkedLocation;

    // Internal state:
    @NonNull
    private final PublishRelay<Object> 

    @NonNull
    private final PublishRelay<Object> showLocation = PublishRelay.create();

    @NonNull
    private final PublishRelay<Object> showLayers = PublishRelay.create();

    @NonNull
    private final PublishRelay<Object> clearLocation = PublishRelay.create();

    @NonNull
    private final PublishRelay<LatLng> shouldMarkLocation = PublishRelay.create();

    @NonNull
    private final Observable<Object> onShowLayers = showLayers.hide();

    @NonNull
    private final Observable<Object> onClearLocation = clearLocation.hide();

    @Inject
    GeoViewModel(@NonNull InitialState initialState,
                 @NonNull WatchPosition watchPosition,
                 @NonNull MarkedLocation markedLocation,
                 @NonNull InfoText infoText,
                 @NonNull StatusText statusText) {

        this.initialState = initialState;
        this.watchPosition = watchPosition;
        this.markedLocation = markedLocation;
        this.infoText = infoText;
        this.statusText = statusText;

        // Observe Location:
        this.hasCurrentPosition = watchPosition.observeLocation()
                .map(Optional::isPresent)
                .distinctUntilChanged()
                .replay(1)
                .refCount();

        Observable<Boolean> hasInitialLocation = initialState.location()
                .doOnNext(optional -> Timber.i("HasInitialLocation: received location, %s", optional))
                .map(Optional::isPresent)
                .doOnNext(__ -> Timber.i("HasInitialLocation: is present: %s.", __))
                .distinctUntilChanged()
                .replay(1)
                .refCount();

        Observable<Boolean> firstLocationReceived = watchPosition.observeLocation()
                .map(Optional::isPresent)
                .distinctUntilChanged()
                .filter(Rx::isTrue);

        this.locationInfoVisibility = hasInitialLocation
                .map(hasLocation -> hasLocation
                        ? View.GONE
                        : View.VISIBLE)
                .distinctUntilChanged();

        this.locationStatusVisibility = hasInitialLocation
                .map(hasLocation -> hasLocation
                        ? View.GONE
                        : View.VISIBLE)
                .distinctUntilChanged();

        this.shouldShowGpsAlert = watchPosition.observeAvailability()
                .filter(isAvailable -> !isAvailable)
                .map(__ -> this);

        this.shouldShowZoomDialog = Observable.combineLatest(showLocation.hide(), firstLocationReceived, Rx::consume)
                .withLatestFrom(watchPosition.observeLocation(), Rx::takeRight)
                .doOnNext(__ -> Timber.i("ShouldShowZoomDialog: Got position."))
                .withLatestFrom(markedLocation.observe(), (current, marked) ->
                        new ZoomData(current.orNull(), marked.orNull()));


        Observable<Boolean> hasMarkedLocation = markedLocation.observe()
                .map(Optional::isPresent)
                .distinctUntilChanged();

        this.isShowLocationEnabled = Observable.combineLatest(hasCurrentPosition,
                hasMarkedLocation,
                Rx::or

        ).distinctUntilChanged();

        // Returns either the Initial location or the first location received from the GPS:
        Observable<LatLng> shouldMarkInitialLocation = Observable.amb(ImmutableList.of(
                initialState.location()
                        .filter(Optional::isPresent)
                        .map(Optional::get),

                firstLocationReceived
                        .combineLatest(watchPosition.observeLocation(), Rx::takeRight)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(this::locationToLatLng)
        ));

        this.onMarkedLocation = Observable.merge(
                shouldMarkInitialLocation,
                shouldMarkLocation.hide()
        );
    }

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        initialState.set(bundle);
    }

    @NonNull
    Observable<String> observeLocationInfoText() {
        return infoText;
    }

    @NonNull
    Observable<String> observeLocationStatusText() {
        return statusText;
    }

    @NonNull
    Observable<Integer> observeLocationInfoVisibility() {
        return locationInfoVisibility;
    }

    @NonNull
    Observable<Integer> observeLocationStatusVisibility() {
        return locationStatusVisibility;
    }

    @NonNull
    Observable<Integer> observePauseButtonVisibility() {
        return Observable.just(View.GONE);
    }

    @NonNull
    Observable<Boolean> observeAddLocationEnabled() {
        return hasCurrentPosition;
    }

    @NonNull
    Observable<Boolean> observeShowLocationEnabled() {
        return isShowLocationEnabled;
    }

    @NonNull
    Observable<Object> observeShowGpsAlert() {
        return shouldShowGpsAlert;
    }

    @NonNull
    Observable<ZoomData> observeShowZoomDialog() {
        return shouldShowZoomDialog;
    }

    @NonNull
    Observable<Object> observeShowLayers() {
        return onShowLayers;
    }

    @NonNull
    Observable<LatLng> observeMarkedLocation() {
        return onMarkedLocation;
    }

    @NonNull
    Observable<Object> observeLocationCleared() {
        return onClearLocation;
    }

    // Inputs:

    Completable addLocation() {
        return watchPosition.observeLocation()
                .doOnNext(locationOptional -> Timber.i("Hmm"))
                .doOnError(throwable -> Timber.w("WTF"))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::locationToLatLng)
                .flatMapCompletable(position -> Completable.defer(() -> {
                    shouldMarkLocation.accept(position);
                    return Completable.complete();
                }));
    }

    Completable pause() {
        return Completable.complete();
    }

    Completable showLocation() {
        return watchPosition.observeLocation()
                .doOnNext(__ -> Timber.i("Calling should show zoom dialog."))
                .doOnNext(showLocation)
                .flatMapCompletable(__ -> Completable.complete());
    }

    Completable showLayers() {
        return Completable.defer(() -> {
            showLayers.accept(this);
            return Completable.complete();
        });
    }

    Completable clearLocation() {
        return Completable.defer(() -> {
            clearLocation.accept(this);
            return Completable.complete();
        });
    }

    void markLocation(@NonNull LatLng latLng) {
        markedLocation.update(latLng);
    }

    void clearMarkedLocation() {
        markedLocation.clear();
    }

    Maybe<String> saveLocation() {
        return Maybe.empty();
    }

    void startWatchingLocation() {
        watchPosition.startWatching();
    }

    void stopWatchingLocation() {
        watchPosition.stopWatching();
    }

    @NonNull
    private LatLng locationToLatLng(@NonNull Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
