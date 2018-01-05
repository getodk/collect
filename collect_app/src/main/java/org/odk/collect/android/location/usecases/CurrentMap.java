package org.odk.collect.android.location.usecases;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.injection.config.scopes.PerActivity;
import org.odk.collect.android.spatial.MapHelper;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

@PerActivity
public class CurrentMap {

    @NonNull
    private final BehaviorRelay<GoogleMap> mapRelay = BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<MapHelper> helperRelay = BehaviorRelay.create();

    @NonNull
    private final Context context;

    @Inject
    CurrentMap(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    public Observable<GoogleMap> observe() {
        return mapRelay.hide();
    }

    @NonNull
    public Observable<MapHelper> observeHelper() {
        return helperRelay.hide();
    }

    @NonNull
    public Single<GoogleMap> get() {
        return mapRelay.firstOrError();
    }

    public void update(@NonNull GoogleMap googleMap) {
        MapHelper helper = new MapHelper(context, googleMap);
        helper.setBasemap();

        mapRelay.accept(googleMap);
        helperRelay.accept(helper);
    }
}
