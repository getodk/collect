package org.odk.collect.android.location.injection;

import android.app.Activity;
import android.support.v4.app.FragmentManager;

import org.odk.collect.android.injection.scopes.ActivityScope;
import org.odk.collect.android.location.GeoActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class GeoActivityModule {

    @ActivityScope
    @Provides
    Activity provideActivity(GeoActivity activity) {
        return activity;
    }

    @ActivityScope
    @Provides
    FragmentManager provideSupportFragmentManager(GeoActivity activity) {
        return activity.getSupportFragmentManager();
    }
}
