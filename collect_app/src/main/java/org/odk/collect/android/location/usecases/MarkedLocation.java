package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerApplication
public class MarkedLocation {

    @NonNull
    private final BehaviorRelay<Optional<LatLng>> markedLocationRelay =
            BehaviorRelay.create();

    @Inject
    MarkedLocation() {

    }

    public Observable<Optional<LatLng>> observe() {
        return markedLocationRelay.hide();
    }

    public void update(@NonNull LatLng latLng) {
        markedLocationRelay.accept(Optional.of(latLng));
    }

    public void clear() {
        markedLocationRelay.accept(Optional.absent());
    }
}
