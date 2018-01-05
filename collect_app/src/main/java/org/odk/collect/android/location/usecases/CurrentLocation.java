package org.odk.collect.android.location.usecases;


import android.location.Location;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerViewModel
public class CurrentLocation {

    private final WatchLocation watchLocation;
    private final IsLocationValid isLocationValid;

    @Inject
    CurrentLocation(@NonNull WatchLocation watchLocation, IsLocationValid isLocationValid) {
        this.watchLocation = watchLocation;
        this.isLocationValid = isLocationValid;
    }

    public Observable<Location> observe() {
        return watchLocation.observeLocation()
                .filter(isLocationValid::isValid);
    }

    private boolean isLocationRecent(@NonNull Location location) {
        long millis = DateTime.now().minus(location.getTime()).getMillis();
        return millis <= 5 * 1_000;
    }
}
