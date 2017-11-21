package org.odk.collect.android.injection;

import android.support.annotation.NonNull;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.location.injection.GeoActivityComponent;

import dagger.Module;

@Module(subcomponents = GeoActivityComponent.class)
public class AppModule {

    @NonNull
    private final Collect application;

    public AppModule(@NonNull Collect application) {
        this.application = application;
    }
}
