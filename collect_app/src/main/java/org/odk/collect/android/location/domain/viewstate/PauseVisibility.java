package org.odk.collect.android.location.domain.viewstate;

import android.support.annotation.NonNull;
import android.view.View;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.model.MapFunction;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerActivity
public class PauseVisibility {

    @NonNull
    private final MapFunction mapFunction;

    @Inject
    PauseVisibility(@NonNull MapFunction mapFunction) {
        this.mapFunction = mapFunction;
    }

    public Observable<Integer> observe() {
        return mapFunction != MapFunction.POINT
                ? Observable.just(View.VISIBLE)
                : Observable.just(View.GONE);
    }
}
