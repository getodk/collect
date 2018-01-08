package org.odk.collect.android.location.usecases;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;

import static org.odk.collect.android.widgets.GeoPointWidget.DRAGGABLE_ONLY;

/**
 * @author James Knight
 */

@PerViewModel
public class IsDraggable {

    private final boolean isDraggable;

    @Inject
    IsDraggable(@NonNull Bundle extras) {
        isDraggable = extras.getBoolean(DRAGGABLE_ONLY);
    }

    @NonNull
    public Observable<Boolean> observe() {
        return Observable.just(isDraggable);
    }
}
