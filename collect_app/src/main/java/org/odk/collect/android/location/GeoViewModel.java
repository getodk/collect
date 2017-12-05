package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.architecture.rx.RxMVVMViewModel;
import org.odk.collect.android.injection.config.scopes.PerViewModel;
import org.odk.collect.android.location.usecases.CurrentLocation;
import org.odk.collect.android.location.usecases.GetAnswer;
import org.odk.collect.android.location.usecases.ReadParameters;

import javax.annotation.Nullable;
import javax.inject.Inject;

import io.reactivex.Completable;
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

    private BehaviorRelay<Boolean> isPauseVisible = BehaviorRelay.createDefault(false);
    private BehaviorRelay<Boolean> isReloadEnabled = BehaviorRelay.createDefault(false);
    private BehaviorRelay<Boolean> isShowEnabled = BehaviorRelay.createDefault(false);

    @Inject
    GeoViewModel(@NonNull GetAnswer getAnswer,
                 @NonNull CurrentLocation currentLocation,
                 @NonNull ReadParameters readParameters) {
        this.getAnswer = getAnswer;
        this.currentLocation = currentLocation;
        this.readParameters = readParameters;
    }

    @Override
    protected void onCreate(@Nullable Bundle parameters) {
        super.onCreate(parameters);
        readParameters.get(parameters);

        currentLocation.observe()
                .compose(bindToLifecycle())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribe(currentLocation -> {
//                    locationInfo.setVisibility(View.GONE);
//                    locationStatus.setVisibility(View.GONE);
//                    showLocation.setEnabled(true);
//                    markerOptions.position(latLng);
//                    marker = map.addMarker(markerOptions);
//                    captureLocation = true;
//                    foundFirstLocation = true;
//                    zoomToPoint();
//
//                    if (!locationClient.isMonitoringLocation() || !isMapReady) {
//                        return;
//                    }
//
//                    // Make sure we can access Location:
//                    if (!locationClient.isLocationAvailable()) {
//                        showGPSDisabledAlertToUser();
//
//                    } else {
//                        if (draggable && !readOnly) {
//                            map.setOnMarkerDragListener(this);
//                            map.setOnMapLongClickListener(this);
//
//                            if (marker != null) {
//                                marker.setDraggable(true);
//                            }
//                        }
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

    Observable<Boolean> isPauseVisible() {
        return isPauseVisible.hide();
    }

    Observable<Boolean> isReloadEnabled() {
        return isReloadEnabled.hide();
    }

    Observable<Boolean> isShowEnabled() {
        return isShowEnabled.hide();
    }

    Completable addLocation() {
        return Completable.complete();
    }

    Completable pause() {
        return Completable.complete();
    }

    Completable showLocation() {
        return Completable.complete();
    }

    Completable showLayers() {
        return Completable.complete();
    }

    Completable clearLocation() {
        return Completable.complete();
    }

    Maybe<String> saveLocation() {
        return getAnswer.get();
    }

}
