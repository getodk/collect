package org.odk.collect.android.location.domain;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.scopes.ActivityScope;

import javax.inject.Inject;

/**
 * @author James Knight
 */

@ActivityScope
public class GetMap {

    @NonNull
    private final FragmentManager fragmentManager;

    @Inject
    public GetMap(@NonNull FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void getAsync(@NonNull OnMapReadyCallback onMapReadyCallback) {
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.gmap);
        mapFragment.getMapAsync(onMapReadyCallback);
    }
}
