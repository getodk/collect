package org.odk.collect.android.injection.architecture;


import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import org.odk.collect.android.injection.keys.ViewModelKey;
import org.odk.collect.android.location.GeoViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(GeoViewModel.class)
    abstract ViewModel bindGeoViewModel(GeoViewModel welcomeViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);
}