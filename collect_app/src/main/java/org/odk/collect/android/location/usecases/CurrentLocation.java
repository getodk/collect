package org.odk.collect.android.location.usecases;


import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;

import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerApplication
public class CurrentLocation {

    private final WatchLocation watchLocation;
    private final IsLocationValid isLocationValid;

    @Nullable
    private Observable<Optional<Location>> cachedObservable = null;

    @Inject
    CurrentLocation(@NonNull WatchLocation watchLocation, IsLocationValid isLocationValid) {
        this.watchLocation = watchLocation;
        this.isLocationValid = isLocationValid;
    }

    @NonNull
    public Observable<Optional<Location>> observe() {
        if (cachedObservable == null) {
            cachedObservable = watchLocation.observeLocation()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(isLocationValid::isValid)
                    .map(Optional::of)
                    .startWith(Optional.absent());
        }

        return cachedObservable;
    }
}
