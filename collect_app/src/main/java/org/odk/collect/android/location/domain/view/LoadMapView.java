package org.odk.collect.android.location.domain.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.maps.SupportMapFragment;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.injection.Qualifiers.IsDraggable;
import org.odk.collect.android.location.mapview.GoogleMapView;
import org.odk.collect.android.location.mapview.MapView;
import org.odk.collect.android.spatial.MapHelper;

import javax.inject.Inject;

import io.reactivex.Single;

@PerActivity
public class LoadMapView {

    @NonNull
    private final Context context;

    @NonNull
    private final FragmentManager fragmentManager;

    @NonNull
    private final SupportMapFragment mapFragment;

    private final boolean isDraggable;

    @Inject
    LoadMapView(@NonNull Context context,
                @NonNull FragmentManager fragmentManager,
                @NonNull SupportMapFragment mapFragment,
                @IsDraggable boolean isDraggable) {

        this.context = context;
        this.fragmentManager = fragmentManager;
        this.mapFragment = mapFragment;
        this.isDraggable = isDraggable;
    }

    public Single<MapView> load() {
        return Single.create(emitter -> {
                    mapFragment.getMapAsync(googleMap -> {
                        googleMap.setMyLocationEnabled(true);
                        googleMap.getUiSettings().setCompassEnabled(true);
                        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        googleMap.getUiSettings().setZoomControlsEnabled(false);

                        MapHelper helper = new MapHelper(context, googleMap);
                        helper.setBasemap();

                        emitter.onSuccess(new GoogleMapView(context, googleMap, isDraggable));
                    });

                    fragmentManager.beginTransaction()
                            .replace(R.id.map_container, mapFragment)
                            .commit();
                }
        );
    }
}
