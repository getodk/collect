package org.odk.collect.android.injection;

import org.odk.collect.android.injection.scopes.ActivityScope;
import org.odk.collect.android.location.GeoActivity;
import org.odk.collect.android.location.injection.GeoActivityComponent;
import org.odk.collect.android.location.injection.GeoActivityModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(subcomponents = GeoActivityComponent.class)
abstract class ActivityBuilder {

    @ActivityScope
    @ContributesAndroidInjector(modules = GeoActivityModule.class)
    abstract GeoActivity bindGeoActivity();
}
