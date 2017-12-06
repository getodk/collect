package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.architecture.rx.RxMVVMViewModel;
import org.odk.collect.android.injection.config.scopes.PerViewModel;
import org.odk.collect.android.location.usecases.CurrentLocation;
import org.odk.collect.android.location.usecases.GetAnswer;
import org.odk.collect.android.location.usecases.ReadParameters;
import org.odk.collect.android.location.usecases.ShowGpsDisabledAlert;
import org.odk.collect.android.location.usecases.WatchLocation;

import javax.annotation.Nullable;
import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Observable;


@PerViewModel
public class GeoViewModel extends RxMVVMViewModel {

    @NonNull
    private final GetAnswer getAnswer;

    @NonNull
    private final CurrentLocation currentLocation;

    @NonNull
    private final ReadParameters readParameters;

    @NonNull
    private final WatchLocation watchLocation;

    @NonNull
    private final ShowGpsDisabledAlert showGpsDisabledAlert;

    private BehaviorRelay<Boolean> isPauseVisible = BehaviorRelay.createDefault(false);
    private BehaviorRelay<Boolean> isReloadEnabled = BehaviorRelay.createDefault(false);
    private BehaviorRelay<Boolean> isShowEnabled = BehaviorRelay.createDefault(false);

    private PublishRelay<LatLng> shouldZoomToLatLng = PublishRelay.create();

    @Inject
    GeoViewModel(@NonNull GetAnswer getAnswer,
                 @NonNull CurrentLocation currentLocation,
                 @NonNull ReadParameters readParameters,
                 @NonNull WatchLocation watchLocation,
                 @NonNull ShowGpsDisabledAlert showGpsDisabledAlert) {
        this.getAnswer = getAnswer;
        this.currentLocation = currentLocation;
        this.readParameters = readParameters;
        this.watchLocation = watchLocation;
        this.showGpsDisabledAlert = showGpsDisabledAlert;
    }

    @Override
    protected void onCreate(@Nullable Bundle parameters) {
        super.onCreate(parameters);
        readParameters.get(parameters);

        watchLocation.observeAvailability()
                .compose(bindToLifecycle())
                .subscribe(isAvailable -> {
                    if (!isAvailable) {
                        showGpsDisabledAlert.show();
                    } else {

//                        if (draggable && !readOnly) {
//                            map.setOnMarkerDragListener(this);
//                            map.setOnMapLongClickListener(this);
//
//                            if (marker != null) {
//                                marker.setDraggable(true);
//                            }
//                        }
                    }
                });

        currentLocation.observe()
                .compose(bindToLifecycle())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribe(currentLocation -> {
                    // THIS IS ON START:

                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    markerOptions.position(latLng);

//                    Marker marker = map.addMarker(markerOptions);
                    shouldZoomToLatLng.accept(latLng);

                    // THIS IS ON NEW LOCATIONS:
//                    if (setClear) {
//                        reloadLocation.setEnabled(true);
//                    }
//
//                    Location previousLocation = this.location;
//                    this.location = location;
//
//                    if (location != null) {
//                        Timber.i("onLocationChanged(%d) location: %s", locationCount, location);
//
//                        if (previousLocation != null) {
//                            enableShowLocation(true);
//
//                            if (!captureLocation && !setClear) {
//                                latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                                markerOptions.position(latLng);
//                                marker = map.addMarker(markerOptions);
//                                captureLocation = true;
//                                reloadLocation.setEnabled(true);
//                            }
//
//                            if (!foundFirstLocation) {
//                                zoomToPoint();
//                                showZoomDialog();
//                                foundFirstLocation = true;
//                            }
//
//                            String locationString = getAccuracyStringForLocation(location);
//                            locationStatus.setText(locationString);
//                        }
//
//                    } else {
//                        Timber.i("onLocationChanged(%d) null location", locationCount);
//                    }
                });
    }

    Observable<Boolean> isLocationStatusVisible() {
        return currentLocation.observe()
                .map(Optional::isPresent);
    }

    Observable<Boolean> isLocationInfoVisible() {
        return currentLocation.observe()
                .map(Optional::isPresent);
    }

    Observable<Boolean> isShowLocationEnabled() {
        return currentLocation.observe()
                .map(Optional::isPresent);
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

    void startWatchingLocation() {
        watchLocation.startWatching();
    }

    void stopWatchingLocation() {
        watchLocation.stopWatching();
    }

    void setLocationAtLatLng(@NonNull LatLng latLng) {
//        this.latLng = latLng;
//        if (marker == null) {
//            markerOptions.position(latLng);
//            marker = map.addMarker(markerOptions);
//        } else {
//            marker.setPosition(latLng);
//        }
//        enableShowLocation(true);
//        marker.setDraggable(true);
//        isDragged = true;
//        setClear = false;
//        captureLocation = true;
    }

    Maybe<String> saveLocation() {
        return getAnswer.get();
    }

}
