package org.odk.collect.android.location.injection;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.odk.collect.android.injection.scopes.ActivityScope;
import org.odk.collect.android.location.GeoViewModel;
import org.odk.collect.android.location.domain.ClearLocation;
import org.odk.collect.android.location.domain.GetMap;
import org.odk.collect.android.location.domain.ReloadLocation;
import org.odk.collect.android.location.domain.SaveLocation;
import org.odk.collect.android.location.domain.SetupMap;
import org.odk.collect.android.location.domain.ShowLayers;
import org.odk.collect.android.location.domain.ShowLocation;

import javax.inject.Inject;

/**
 * @author James Knight
 */

@ActivityScope
public class GeoViewModelFactory implements ViewModelProvider.Factory {

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

    @Inject
    public GeoViewModelFactory(@NonNull GetMap getMap,
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

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GeoViewModel.class)) {
            //noinspection unchecked
            return (T) new GeoViewModel(getMap, setupMap, reloadLocation, showLocation, showLayers, clearLocation, saveLocation);
        }

        throw new IllegalArgumentException("Unknown ViewModel class!");
    }
}
