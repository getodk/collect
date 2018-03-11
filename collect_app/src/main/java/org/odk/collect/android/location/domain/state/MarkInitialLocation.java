package org.odk.collect.android.location.domain.state;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.domain.utility.LocationConverter;
import org.odk.collect.android.location.injection.Qualifiers.InitialLocation;
import org.odk.collect.android.location.injection.Qualifiers.IsDraggable;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import io.reactivex.Observable;
import timber.log.Timber;

@PerActivity
public class MarkInitialLocation {

    @Nullable
    private final LatLng initialLocation;

    @NonNull
    private final CurrentLocation currentLocation;

    private final boolean isDraggable;

    @Inject
    MarkInitialLocation(@NonNull CurrentLocation currentLocation,
                        @InitialLocation @Nullable LatLng initialLocation,
                        @IsDraggable boolean isDraggable) {
        this.currentLocation = currentLocation;
        this.initialLocation = initialLocation;
        this.isDraggable = isDraggable;
    }

    public Observable<LatLng> observe() {
        if (initialLocation != null) {
            return Observable.just(initialLocation);
        }

        if (!isDraggable) {
            return currentLocation.observePresence()
                    .filter(Rx::isTrue)
                    .distinctUntilChanged()
                    .flatMapSingle(__ -> currentLocation.get())
                    .map(Optional::get)
                    .map(LocationConverter::locationToLatLng);
        }

        return Observable.empty();
    }
}
