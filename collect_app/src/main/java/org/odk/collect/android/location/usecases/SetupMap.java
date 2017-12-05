package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;

import org.odk.collect.android.injection.config.scopes.PerActivity;

import javax.inject.Inject;

import io.reactivex.Completable;

@PerActivity
public class SetupMap {

    @Inject
    public SetupMap() {
    }

    public Completable setup(@NonNull GoogleMap googleMap) {

        return Completable.create(emitter -> {

        });
    }
}
