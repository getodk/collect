package org.odk.collect.android.location.usecases;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

import io.reactivex.Observable;

import static org.odk.collect.android.widgets.GeoPointWidget.DRAGGABLE_ONLY;
import static org.odk.collect.android.widgets.GeoPointWidget.LOCATION;
import static org.odk.collect.android.widgets.GeoPointWidget.READ_ONLY;

@PerApplication
public class InitialState {

    @NonNull
    private final BehaviorRelay<Boolean> isReadOnlyRelay = BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<Boolean> isDraggableRelay = BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<Optional<LatLng>> locationRelay = BehaviorRelay.create();

    @Inject
    InitialState() {

    }

    public void set(@Nullable Bundle bundle) {
        bundle = bundle != null
                ? bundle
                : Bundle.EMPTY;

        isReadOnlyRelay.accept(bundle.getBoolean(READ_ONLY, false));
        isDraggableRelay.accept(bundle.getBoolean(DRAGGABLE_ONLY, false));

        double[] location = bundle.getDoubleArray(LOCATION);
        LatLng latLng = location != null
                ? new LatLng(location[0], location[1])
                : null;

        locationRelay.accept(Optional.fromNullable(latLng));
    }


    @NonNull
    public Observable<Boolean> isReadOnly() {
        return isReadOnlyRelay.hide();
    }

    @NonNull
    public Observable<Boolean> isDraggable() {
        return isDraggableRelay.hide();
    }

    public Observable<Optional<LatLng>> location() {
        return locationRelay.hide();
    }
}
