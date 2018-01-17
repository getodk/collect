package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;

import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerApplication
public class InitialLocation {

    @NonNull
    private final Observable<Optional<LatLng>> observeLocation;

    @NonNull
    private final Observable<Boolean> observePresence;

    @Inject
    InitialLocation(@NonNull InitialState initialState) {
        observeLocation = initialState.location();
        observePresence = observeLocation.map(Optional::isPresent);
    }

    public Observable<Optional<LatLng>> observe() {
        return observeLocation;
    }

    public Observable<Boolean> observePresence() {
        return observePresence;
    }
}
