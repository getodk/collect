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
    private final LoadMap loadMap;

    @Inject
    public ShowLayers(@NonNull Context context, @NonNull LoadMap loadMap) {
        this.context = context;
        this.loadMap = loadMap;
    }

    private Completable show() {
        return loadMap.load()
                .map(googleMap -> new MapHelper(context, googleMap))
                .flatMapCompletable(mapHelper -> {
                    mapHelper.showLayersDialog(context);
                    return Completable.complete();
                });
    }
}
