package org.odk.collect.android.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.R;
import org.odk.collect.android.architecture.rx.RxMvvmViewModel;
import org.odk.collect.android.location.model.MapFunction;
import org.odk.collect.android.location.model.MapType;
import org.odk.collect.android.location.model.ZoomData;
import org.odk.collect.android.location.viewmodel.LocationFormatter;
import org.odk.collect.android.location.viewmodel.WatchPosition;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

import static org.odk.collect.android.widgets.GeoPointWidget.DRAGGABLE_ONLY;
import static org.odk.collect.android.widgets.GeoPointWidget.LOCATION;
import static org.odk.collect.android.widgets.GeoPointWidget.READ_ONLY;


public class GeoViewModel extends RxMvvmViewModel {

    public static final String MAP_TYPE = "map_type";
    public static final String MAP_FUNCTION = "map_function";

    // Inputs:
    @NonNull
    private final WatchPosition watchPosition;

    // Outputs:
    @NonNull
    private final Observable<Boolean> isShowLocationEnabled;

    @NonNull
    private final Observable<Object> shouldShowGpsAlert;

    @NonNull
    private final Observable<ZoomData> shouldShowZoomDialog;

    @NonNull
    private final Observable<LatLng> onMarkedLocation;

    // Initial state:

    @NonNull
    private final BehaviorRelay<Boolean> isReadOnlyRelay = BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<Boolean> isDraggableRelay = BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<Optional<LatLng>> locationRelay = BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<MapType> typeRelay = BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<MapFunction> functionRelay = BehaviorRelay.create();

    @NonNull
    private final Observable<Boolean> isReadOnly = isReadOnlyRelay.hide();

    @NonNull
    private final Observable<Boolean> isDraggable = isDraggableRelay.hide();

    @NonNull
    private final Observable<Optional<LatLng>> initialLocation = locationRelay.hide();

    @NonNull
    private final Observable<MapType> mapType = typeRelay.hide();

    @NonNull
    private final Observable<MapFunction> mapFunction = functionRelay.hide();

    // Variables:

    @NonNull
    private final BehaviorRelay<Optional<LatLng>> selectedLocationRelay =
            BehaviorRelay.createDefault(Optional.absent());

    @NonNull
    private final Observable<Optional<LatLng>> observeSelectedLocation =
            selectedLocationRelay.hide();

    @NonNull
    private final BehaviorRelay<Boolean> hasCurrentPosition = BehaviorRelay.createDefault(false);

    @NonNull
    private final Observable<Boolean> observeHasCurrentPosition = hasCurrentPosition.hide();

    @NonNull
    private final Observable<Boolean> hasInitialLocation;

    @NonNull
    private final Observable<Boolean> hasSelectedLocation;

    @NonNull
    private final Observable<String> observeLocationInfoText;

    @NonNull
    private final Observable<String> observeLocationStatusText;

    @NonNull
    private final Observable<Integer> observeLocationInfoVisibility;

    @NonNull
    private final Observable<Integer> observeLocationStatusVisibility;

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

    @NonNull
    private final BehaviorRelay<Boolean> hasBeenCleared =
            BehaviorRelay.createDefault(false);

    @Inject
    GeoViewModel(@NonNull Context context,
                 @NonNull WatchPosition watchPosition,
                 @NonNull LocationFormatter locationFormatter) {

        this.watchPosition = watchPosition;

        observeLocationInfoText = isDraggable()
                .map(isDraggable -> isDraggable
                        ? R.string.geopoint_instruction
                        : R.string.geopoint_no_draggable_instruction)
                .map(context::getString);

        observeLocationInfoVisibility = Observable.combineLatest(
                isReadOnly,
                initialLocation.map(Optional::isPresent),
                hasBeenCleared.hide(),
                (isReadOnly, hasInitial, hasBeenCleared) ->
                        isReadOnly || (hasInitial && !hasBeenCleared))
                .map(shouldHide -> shouldHide ? View.GONE : View.VISIBLE);

        observeLocationStatusText = watchPosition.observeLocation()
                .map(currentLocation -> currentLocation.isPresent()
                        ? locationFormatter.getStringForLocation(currentLocation.get())
                        : context.getString(R.string.please_wait_long));

        observeLocationStatusVisibility = Observable.combineLatest(
                isReadOnly,
                initialLocation.map(Optional::isPresent),
                hasBeenCleared.hide(),
                (isReadOnly, hasInitial, hasBeenCleared) ->
                        isReadOnly || (hasInitial && !hasBeenCleared))
                .map(shouldHide -> shouldHide ? View.GONE : View.VISIBLE);


        // Observe Location:
        hasInitialLocation = initialLocation
                .map(Optional::isPresent)
                .distinctUntilChanged();

        hasSelectedLocation = observeSelectedLocation
                .map(Optional::isPresent)
                .distinctUntilChanged();

        shouldShowGpsAlert = watchPosition.observeAvailability()
                .filter(isAvailable -> !isAvailable)
                .map(__ -> this);

        // Returns either the Initial location or the first location received from the GPS:
        Observable<Object> onFirstMarkedLocation = hasSelectedLocation
                .filter(Rx::isTrue)
                .distinctUntilChanged()
                .map(Rx::toEvent);

        Observable<Object> shouldZoomOnFirstLocation = onFirstMarkedLocation
                .doOnNext(__ -> Timber.i(""))
                .withLatestFrom(hasInitialLocation, Rx::takeRight)
                .withLatestFrom(isDraggable, Rx::or)
                .filter(Rx::isFalse)
                .map(Rx::toEvent);

        shouldShowZoomDialog = Observable.merge(showLocation.hide(), shouldZoomOnFirstLocation)
                .flatMapSingle(__ -> Single.zip(
                        watchPosition.currentLocation(),
                        observeSelectedLocation.firstOrError(),
                        (current, selected) -> new ZoomData(current.orNull(), selected.orNull())
                ))
                .filter(zoomData -> !zoomData.isEmpty());

        isShowLocationEnabled = Observable.combineLatest(
                observeHasCurrentPosition,
                hasSelectedLocation,
                Rx::or
        );

        Observable<LatLng> onFirstLocationNotInitial = observeHasCurrentPosition
                .filter(Rx::isTrue)
                .distinctUntilChanged()
                .flatMapSingle(__ -> watchPosition.currentLocation())
                .withLatestFrom(isDraggable, hasInitialLocation, isReadOnly,
                        (location, draggable, hasInitial, isReadOnly) ->
                                !draggable && !hasInitial && !isReadOnly
                                        ? location
                                        : Optional.<Location>absent()
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::locationToLatLng);

        Observable<LatLng> onInitialLocation = initialLocation
                .filter(Optional::isPresent)
                .map(Optional::get);

        @SuppressWarnings("unchecked")
        Observable<LatLng> shouldMarkInitialLocation = Observable.ambArray(
                onInitialLocation,
                onFirstLocationNotInitial
        );

        onMarkedLocation = Observable.merge(
                shouldMarkInitialLocation,
                shouldMarkLocation.hide()
        );
    }

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);

        watchPosition.observeLocation()
                .compose(bindToLifecycle())
                .map(Optional::isPresent)
                .subscribe(hasCurrentPosition, Timber::e);
    }

    // UI state:
    @NonNull
    Observable<String> locationInfoText() {
        return observeLocationInfoText;
    }

    @NonNull
    Observable<String> locationStatusText() {
        return observeLocationStatusText;
    }

    @NonNull
    Observable<Integer> locationInfoVisibility() {
        return observeLocationInfoVisibility;
    }

    @NonNull
    Observable<Integer> locationStatusVisibility() {
        return observeLocationStatusVisibility;
    }

    @NonNull
    Observable<Integer> pauseButtonVisibility() {
        return Observable.just(View.GONE);
    }

    @NonNull
    Observable<Boolean> isAddLocationEnabled() {
        return Observable.combineLatest(
                isReadOnly,
                observeHasCurrentPosition,
                (isReadOnly, hasCurrentLocation) ->
                        !isReadOnly && hasCurrentLocation
        );
    }

    @NonNull
    Observable<Boolean> isShowLocationEnabled() {
        return isShowLocationEnabled;
    }

    @NonNull
    Observable<Boolean> isClearLocationEnabled() {
        return Observable.combineLatest(isReadOnly, hasInitialLocation, hasSelectedLocation,
                (isReadOnly, hasInitialLocation, hasSelectedLocation) ->
                        !isReadOnly && (hasInitialLocation || hasSelectedLocation)
        );
    }

    @NonNull
    Observable<Boolean> isDraggable() {
        return isDraggable;
    }

    // Events:
    @NonNull
    Observable<LatLng> onLocationAdded() {
        return onMarkedLocation;
    }

    @NonNull
    Observable<Object> onShowGpsAlert() {
        return shouldShowGpsAlert;
    }

    @NonNull
    Observable<ZoomData> onShowZoomDialog() {
        return shouldShowZoomDialog;
    }

    @NonNull
    Observable<Object> onShowLayers() {
        return onShowLayers;
    }

    @NonNull
    Observable<Object> onLocationCleared() {
        return onClearLocation;
    }

    @NonNull
    Observable<LatLng> onInitialLocation() {
        return initialLocation
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    // Inputs:

    Completable addLocation() {
        return isReadOnly.flatMapCompletable(isReadOnly -> {
            if (isReadOnly) {
                return Completable.complete();
            }

            return watchPosition.currentLocation()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(this::locationToLatLng)
                    .doOnSuccess(shouldMarkLocation)
                    .flatMapCompletable(__ -> Completable.complete());
        });
    }

    Completable pause() {
        return Completable.complete();
    }

    Completable showLocation() {
        return Completable.defer(() -> {
            showLocation.accept(this);
            return Completable.complete();
        });
    }

    Completable showLayers() {
        return Completable.defer(() -> {
            showLayers.accept(this);
            return Completable.complete();
        });
    }

    Completable clearLocation() {
        return isReadOnly
                .flatMapCompletable(isReadOnly -> {
                    if (!isReadOnly) {
                        clearLocation.accept(this);
                    }

                    return Completable.complete();
                });
    }

    Completable selectLocation(@NonNull LatLng latLng) {
        return isReadOnly
                .flatMapCompletable(isReadOnly -> {
                    if (isReadOnly) {
                        return initialLocation.firstOrError()
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .flatMapCompletable(initialLatLng -> {
                                    if (initialLatLng.equals(latLng)) {
                                        selectedLocationRelay.accept(Optional.of(latLng));
                                    }

                                    return Completable.complete();
                                });
                    }

                    selectedLocationRelay.accept(Optional.of(latLng));
                    return Completable.complete();
                });
    }

    Completable clearSelectedLocation() {
        return Completable.defer(() -> {
            selectedLocationRelay.accept(Optional.absent());
            hasBeenCleared.accept(true);
            return Completable.complete();
        });
    }

    Single<String> saveLocation() {
        return Single.just("");
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

    @Override
    public void onInitialState(@NonNull Bundle bundle) {
        boolean isReadOnly = bundle.getBoolean(READ_ONLY, false);
        isReadOnlyRelay.accept(isReadOnly);

        boolean isDraggable = bundle.getBoolean(DRAGGABLE_ONLY, false);
        isDraggableRelay.accept(!isReadOnly && isDraggable);

        double[] location = bundle.getDoubleArray(LOCATION);
        LatLng latLng = location != null
                ? new LatLng(location[0], location[1])
                : null;

        locationRelay.accept(Optional.fromNullable(latLng));

        MapType mapType = (MapType) bundle.get(MAP_TYPE);
        typeRelay.accept(mapType != null
                ? mapType
                : MapType.GOOGLE);

        MapFunction mapFunction = (MapFunction) bundle.get(MAP_FUNCTION);
        functionRelay.accept(mapFunction != null
                ? mapFunction
                : MapFunction.TRACE);
    }
}
