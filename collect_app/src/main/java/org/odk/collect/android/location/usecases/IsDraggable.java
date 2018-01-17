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

    @NonNull
    private final IsReadOnly isReadOnly;

    @Inject
    IsDraggable(@NonNull InitialState initialState,
                @NonNull IsReadOnly isReadOnly) {
        this.initialState = initialState;
        this.isReadOnly = isReadOnly;
    }

    @NonNull
    public Observable<Boolean> observe() {
        return Observable.combineLatest(initialState.isDraggable(), isReadOnly.observe(),
                (isDraggable, isReadOnly) -> isDraggable && !isReadOnly);
    }
}
