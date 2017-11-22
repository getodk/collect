package org.odk.collect.android.injection;

import org.odk.collect.android.injection.scopes.ActivityScope;
import org.odk.collect.android.location.GeoActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityBuilder {

    @ActivityScope
    abstract GeoActivity bindGeoActivity();
}
