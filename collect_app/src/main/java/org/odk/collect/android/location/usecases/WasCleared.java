package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;

import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerApplication
public class WasCleared {

    @NonNull
    private final BehaviorRelay<Boolean> hasBeenCleared = BehaviorRelay.createDefault(false);

    @Inject
    WasCleared() {

    }

    @NonNull
    public Observable<Boolean> observe() {
        return hasBeenCleared.hide();
    }

    @NonNull
    public void clear() {
        hasBeenCleared.accept(true);
    }
}
