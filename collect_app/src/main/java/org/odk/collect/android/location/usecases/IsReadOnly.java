package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;

import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

import io.reactivex.Observable;


@PerApplication
public class IsReadOnly {

    @NonNull
    private final InitialState initialState;

    @Inject
    IsReadOnly(@NonNull InitialState initialState) {
        this.initialState = initialState;
    }

    @NonNull
    public Observable<Boolean> observe() {
        return initialState.isReadOnly();
    }
}