package org.odk.collect.android.location;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.odk.collect.android.location.domain.ClearLocation;
import org.odk.collect.android.location.domain.ReloadLocation;
import org.odk.collect.android.location.domain.SaveLocation;
import org.odk.collect.android.location.domain.SetupMap;
import org.odk.collect.android.location.domain.ShowLayers;
import org.odk.collect.android.location.domain.ShowLocation;

import javax.annotation.Nullable;

public class GeoViewModel extends ViewModel implements OnMapReadyCallback {

    public ObservableField<GeoProvider> provider = new ObservableField<>();
    public ObservableField<GeoMode> mode = new ObservableField<>();

    public ObservableField<String> status = new ObservableField<>();

    private SetupMap setupMap;
    private ReloadLocation reloadLocation;
    private ShowLocation showLocation;
    private ShowLayers showLayers;
    private ClearLocation clearLocation;
    private SaveLocation saveLocation;

    @Nullable
    private Location currentLocation = null;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        setupMap.setMap(googleMap);
    }

    public void onReloadLocation() {
        reloadLocation.reload();
    }

    public void onShowLocation() {
        showLocation.showLocation();
    }

    public void onShowLayers() {
        showLayers.showLayers();
    }

    public void onClear() {
        clearLocation.clear();
    }

    public void onAcceptLocation() {
        saveLocation.save(currentLocation);
    }
}
