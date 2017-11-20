package org.odk.collect.android.location;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import org.odk.collect.android.location.domain.ClearLocation;
import org.odk.collect.android.location.domain.GetMap;
import org.odk.collect.android.location.domain.ReloadLocation;
import org.odk.collect.android.location.domain.SaveLocation;
import org.odk.collect.android.location.domain.SetupMap;
import org.odk.collect.android.location.domain.ShowLayers;
import org.odk.collect.android.location.domain.ShowLocation;

import timber.log.Timber;

public class GeoViewModel extends ViewModel {

    @NonNull
    private final ObservableField<GeoProvider> provider = new ObservableField<>();

    @NonNull
    private final ObservableField<GeoMode> mode = new ObservableField<>();

    @NonNull
    private final ObservableField<String> status = new ObservableField<>();

    @NonNull
    private final GetMap getMap;

    @NonNull
    private final SetupMap setupMap;

    @NonNull
    private final ReloadLocation reloadLocation;

    @NonNull
    private final ShowLocation showLocation;

    @NonNull
    private final ShowLayers showLayers;

    @NonNull
    private final ClearLocation clearLocation;

    @NonNull
    private final SaveLocation saveLocation;

    public GeoViewModel(@NonNull GetMap getMap,
                        @NonNull SetupMap setupMap,
                        @NonNull ReloadLocation reloadLocation,
                        @NonNull ShowLocation showLocation,
                        @NonNull ShowLayers showLayers,
                        @NonNull ClearLocation clearLocation,
                        @NonNull SaveLocation saveLocation) {

        this.getMap = getMap;
        this.setupMap = setupMap;
        this.reloadLocation = reloadLocation;
        this.showLocation = showLocation;
        this.showLayers = showLayers;
        this.clearLocation = clearLocation;
        this.saveLocation = saveLocation;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Timber.w("CLEARED!");
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
        saveLocation.save(null);
    }
}
