package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerViewModel
public class MarkedLocation {

    @NonNull
    private final BehaviorRelay<Optional<LatLng>> markedLocation;

    @Inject
    MarkedLocation(@Nullable Optional<LatLng> initialLocation) {
        markedLocation = BehaviorRelay.createDefault(initialLocation);
    }

    public Observable<Optional<LatLng>> observe() {
        return markedLocation.startWith().hide();
    }

    public void update(@Nullable LatLng latLng) {
        markedLocation.accept(Optional.fromNullable(latLng));
    }

    public void clear() {
        markedLocation.accept(Optional.absent());
    }
}
