package org.odk.collect.android.injection.config;

import org.odk.collect.android.injection.config.scopes.PerActivity;
import org.odk.collect.android.location.GeoActivity;
import org.odk.collect.android.location.injection.GeoActivityModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = GeoActivityModule.class)
    @PerActivity
    abstract GeoActivity bindGeoActivity();
}