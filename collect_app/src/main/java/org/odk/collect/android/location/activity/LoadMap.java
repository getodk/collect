package org.odk.collect.android.location.activity;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerActivity;
import org.odk.collect.android.location.GeoActivity;
import org.odk.collect.android.spatial.MapHelper;

import javax.inject.Inject;

import io.reactivex.Single;

@PerActivity
public class LoadMap {

    @NonNull
    private final GeoActivity activity;

    @NonNull
    private final FragmentManager fragmentManager;

    @NonNull
    private final SupportMapFragment mapFragment;

    @Inject
    LoadMap(@NonNull GeoActivity activity,
            @NonNull FragmentManager fragmentManager,
            @NonNull SupportMapFragment mapFragment) {

        this.activity = activity;
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

                MapHelper helper = new MapHelper(activity, googleMap);
                helper.setBasemap();

                emitter.onSuccess(googleMap);
            });

            fragmentManager.beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();
        });
    }
}
