package org.odk.collect.android.location.domain.viewstate;


import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.injection.Qualifiers.InitialLocation;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerActivity
public class OnZoom {

    @Nullable
    private final LatLng initialLocation;

    @Inject
    OnZoom(@InitialLocation @Nullable LatLng initialLocation) {
        this.initialLocation = initialLocation;
    }

    public Observable<LatLng> observe() {
        return initialLocation != null
                ? Observable.just(initialLocation)
                : Observable.empty();
    }
}
