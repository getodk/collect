package org.odk.collect.android.location.usecases;


import android.location.Location;
import android.support.annotation.NonNull;

import com.google.common.base.Optional;

import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerApplication
public class CurrentLocation {

    private final WatchLocation watchLocation;
    private final IsLocationValid isLocationValid;

    @Inject
    CurrentLocation(@NonNull WatchLocation watchLocation, IsLocationValid isLocationValid) {
        this.watchLocation = watchLocation;
        this.isLocationValid = isLocationValid;
    }

    public Observable<Optional<Location>> observe() {
        return watchLocation.observeLocation()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(isLocationValid::isValid)
                .map(Optional::of)
                .startWith(Optional.absent());
    }
}
