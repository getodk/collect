package org.odk.collect.android.location.usecases;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;

import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;

import static org.odk.collect.android.widgets.GeoPointWidget.LOCATION;

@PerViewModel
public class InitialLocation {

    @Nullable
    private final LatLng latLng;

    @Inject
    InitialLocation(@NonNull Bundle extras) {
        double[] locationArray = extras.getDoubleArray(LOCATION);
        latLng = locationArray != null
                ? new LatLng(locationArray[0], locationArray[1])
                : null;
    }

    public Observable<Optional<LatLng>> observe() {
        return Observable.just(Optional.fromNullable(latLng));
    }
}
