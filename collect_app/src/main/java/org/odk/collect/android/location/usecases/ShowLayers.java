package org.odk.collect.android.location.usecases;

import android.content.Context;
import android.support.annotation.NonNull;

import org.odk.collect.android.injection.config.scopes.PerActivity;
import org.odk.collect.android.spatial.MapHelper;

import javax.inject.Inject;

import io.reactivex.Completable;

@PerActivity
public class ShowLayers {

    @NonNull
    private final Context context;

    @NonNull
    private final GetMap getMap;

    @Inject
    public ShowLayers(@NonNull Context context, @NonNull GetMap getMap) {
        this.context = context;
        this.getMap = getMap;
    }

    private Completable show() {
        return getMap.get()
                .map(googleMap -> new MapHelper(context, googleMap))
                .flatMapCompletable(mapHelper -> {
                    mapHelper.showLayersDialog(context);
                    return Completable.complete();
                });
    }
}
