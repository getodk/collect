package org.odk.collect.android.injection;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.GeoActivity;

import dagger.Module;

/**
 * Module for creating injectable Activity subclasses.
 * To add more injectable Activities, copy the GeoActivity format below.
 */
@Module
abstract class ActivityBuilder {

    @PerActivity
    abstract GeoActivity bindGeoActivity();
}
