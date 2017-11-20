package org.odk.collect.android.injection;

import android.support.annotation.NonNull;

import org.odk.collect.android.application.Collect;

import dagger.Module;

@Module
public class AppModule {

    @NonNull
    private final Collect application;

    public AppModule(@NonNull Collect application) {
        this.application = application;
    }
}
