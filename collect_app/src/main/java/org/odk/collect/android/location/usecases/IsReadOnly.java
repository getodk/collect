package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;

import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author James Knight
 */
@PerViewModel
public class IsReadOnly {

    private final boolean isReadOnly;

    @Inject
    IsReadOnly(@NonNull InitialState initialState) {
        isReadOnly = initialState.isReadOnly();
    }

    @NonNull
    public Observable<Boolean> observe() {
        return Observable.just(isReadOnly);
    }
}