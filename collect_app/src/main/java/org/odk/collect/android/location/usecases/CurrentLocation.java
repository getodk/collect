package org.odk.collect.android.location.usecases;


import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Observable;

@PerViewModel
public class CurrentLocation {
    private BehaviorRelay<Optional<Location>> locationRelay =
            BehaviorRelay.createDefault(Optional.absent());

    @Inject
    public CurrentLocation() {

    }

    public Observable<Optional<Location>> observe() {
        return locationRelay.hide();
    }

    public Maybe<Location> get() {
        return locationRelay.firstElement()
                .flatMap(locationOptional -> locationOptional.isPresent()
                        ? Maybe.just(locationOptional.get())
                        : Maybe.empty());
    }

    public void update(@NonNull Location location) {
        locationRelay.accept(Optional.of(location));
    }

    public void clear() {
        locationRelay.accept(Optional.absent());
    }
}
