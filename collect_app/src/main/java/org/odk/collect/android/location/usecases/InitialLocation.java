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
    private final InitialState initialState;

    @Inject
    InitialLocation(@NonNull InitialState initialState) {
        this.initialState = initialState;
    }

    public Observable<Optional<LatLng>> observe() {
        return initialState.location();
    }
}
