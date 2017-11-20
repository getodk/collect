package org.odk.collect.android.injection;

import org.odk.collect.android.location.GeoActivity;
import org.odk.collect.android.location.injection.GeoActivityModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = GeoActivityModule.class)
    abstract GeoActivity bindGeoActivity();
}
