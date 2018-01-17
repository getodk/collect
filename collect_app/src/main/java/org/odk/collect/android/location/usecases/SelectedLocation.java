package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

@PerApplication
public class SelectedLocation {

    @NonNull
    private final BehaviorRelay<Optional<LatLng>> selectedLocationRelay =
            BehaviorRelay.createDefault(Optional.absent());

    @NonNull
    private final Observable<Optional<LatLng>> observeSelectedLocation =
            selectedLocationRelay.hide();
    @Inject
    SelectedLocation() {

    }

    public Observable<Optional<LatLng>> observe() {
        return observeSelectedLocation;
    }

    public Single<Optional<LatLng>> get() {
        return observeSelectedLocation.firstOrError();
    }

    public void update(@NonNull LatLng latLng) {
        selectedLocationRelay.accept(Optional.of(latLng));
    }

    public void clear() {
        selectedLocationRelay.accept(Optional.absent());
    }
}
