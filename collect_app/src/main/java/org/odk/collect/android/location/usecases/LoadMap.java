package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerActivity;

import javax.inject.Inject;

import io.reactivex.Single;

@PerActivity
public class LoadMap {

    @NonNull
    private final FragmentManager fragmentManager;

    @NonNull
    private final SupportMapFragment mapFragment;

    @Inject
    LoadMap(@NonNull FragmentManager fragmentManager,
            @NonNull SupportMapFragment mapFragment) {

        this.fragmentManager = fragmentManager;
        this.mapFragment = mapFragment;
    }

    public Single<GoogleMap> load() {
        return Single.create(emitter -> {
            mapFragment.getMapAsync(googleMap -> {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(false);

                emitter.onSuccess(googleMap);
            });
            fragmentManager.beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();

        });
    }
}
