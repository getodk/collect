package org.odk.collect.android.injection;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import org.odk.collect.android.injection.config.architecture.ViewModelFactory;
import org.odk.collect.android.injection.config.architecture.ViewModelKey;
import org.odk.collect.android.location.GeoViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

/**
 * Module for binding injectable ViewModels.
 * <p>
 * To add a new ViewModel, just copy the format for GeoViewModel below, and Dagger will make sure
 * its injected into your injectable Activity's (see {@link ActivityBuilder}) `@Inject` annotated
 * ViewModel subclass field.
 */
@Module
public abstract class ViewModelBuilder {

    /**
     * Provides our custom ViewModelFactory, do not change this unless absolutely necessary:
     */
    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);

    @Binds
    @IntoMap
    @ViewModelKey(GeoViewModel.class)
    abstract ViewModel bindGeoViewModel(GeoViewModel geoViewModel);
}
