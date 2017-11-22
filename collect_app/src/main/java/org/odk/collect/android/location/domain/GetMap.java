package org.odk.collect.android.location.domain;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.scopes.ActivityScope;
import org.odk.collect.android.location.GeoActivity;

import javax.inject.Inject;

import io.reactivex.Single;

@ActivityScope
public class GetMap {

    @NonNull
    private final FragmentManager fragmentManager;

    @Inject
    GetMap(@NonNull GeoActivity geoActivity) {
        this.fragmentManager = geoActivity.getSupportFragmentManager();
    }

    public Single<GoogleMap> get() {
        return Single.create(emitter -> {
            SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.gmap);
            mapFragment.getMapAsync(googleMap -> {
                if (googleMap == null) {
                    emitter.onError(new IllegalStateException("Couldn't get Google Map."));
                    return;
                }

                emitter.onSuccess(googleMap);
            });
        });
    }
}
