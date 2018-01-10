package org.odk.collect.android.location.usecases;


import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;

import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerApplication
public class CurrentPosition {


    @Nullable
    private Observable<Optional<Location>> observable = null;

    @Inject
    CurrentPosition(@NonNull WatchPosition watchPosition, IsPositionValid isPositionValid) {
        observable = watchPosition.observeLocation()
                .doOnNext(Rx.logi("Position received."))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(isPositionValid::isValid)
                .map(Optional::of);
    }

    @NonNull
    public Observable<Optional<Location>> observe() {
        return observable;
    }
}
