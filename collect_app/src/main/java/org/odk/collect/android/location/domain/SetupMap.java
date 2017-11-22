package org.odk.collect.android.location.domain;

import com.google.android.gms.maps.GoogleMap;

import org.odk.collect.android.injection.scopes.ActivityScope;

import javax.annotation.Nullable;
import javax.inject.Inject;

import io.reactivex.Single;

@ActivityScope
public class SetupMap {

    @Inject
    SetupMap() {
    }

    public Single<GoogleMap> setup(@Nullable final GoogleMap map) {
        return Single.create(emitter -> {
            if (map == null) {
                emitter.onError(new IllegalArgumentException("Can't setup a null GoogleMap."));
                return;
            }

            map.setMyLocationEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);

            emitter.onSuccess(map);
        });
    }
}
