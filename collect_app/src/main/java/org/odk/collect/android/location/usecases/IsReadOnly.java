package org.odk.collect.android.location.usecases;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;

import static org.odk.collect.android.widgets.GeoPointWidget.READ_ONLY;


@PerViewModel
public class IsReadOnly {

    private final boolean isReadOnly;

    @Inject
    IsReadOnly(@NonNull Bundle extras) {
        isReadOnly = extras.getBoolean(READ_ONLY, false);
    }

    @NonNull
    public Observable<Boolean> observe() {
        return Observable.just(isReadOnly);
    }
}