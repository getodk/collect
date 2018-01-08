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

    private final BehaviorRelay<Bundle> initialBundleRelay = BehaviorRelay.create();

    @Inject
    InitialState() {

    }

    public void set(@Nullable Bundle bundle) {
        initialBundleRelay.accept(bundle != null
                ? bundle
                : Bundle.EMPTY);
    }


    @NonNull
    public Observable<Boolean> isReadOnly() {
        return initialBundleRelay.hide()
                .map(bundle -> bundle.getBoolean(READ_ONLY, false));
    }

    @NonNull
    public Observable<Boolean> isDraggable() {
        return initialBundleRelay.hide()
                .map(bundle -> bundle.getBoolean(DRAGGABLE_ONLY, false));
    }

    public Observable<Optional<LatLng>> location() {
        return initialBundleRelay.hide()
                .map(bundle -> {
                    double[] location = bundle.getDoubleArray(LOCATION);
                    LatLng latLng = location != null
                            ? new LatLng(location[0], location[1])
                            : null;

                    return Optional.fromNullable(latLng);
                });
    }
}
