package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;

import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author James Knight
 */

@PerApplication
public class IsDraggable {

    @NonNull
    private final InitialState initialState;

    @Inject
    IsDraggable(@NonNull InitialState initialState) {
        this.initialState = initialState;
    }

    @NonNull
    Observable<Boolean> observe() {
        return initialState.isDraggable();
    }
}
