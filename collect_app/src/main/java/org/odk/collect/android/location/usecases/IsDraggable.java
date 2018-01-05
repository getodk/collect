package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author James Knight
 */

@PerViewModel
public class IsDraggable {

    private final boolean isInitiallyDraggable;
    private final BehaviorRelay<Boolean> isDraggableRelay;

    @Inject
    IsDraggable(@NonNull InitialState initialState) {
        isInitiallyDraggable = initialState.isDraggable();
        isDraggableRelay = BehaviorRelay.createDefault(isInitiallyDraggable);
    }

    @NonNull
    public Observable<Boolean> observe() {
        return isDraggableRelay.hide();
    }

    public void update(boolean isDraggable) {
        isDraggableRelay.accept(isDraggable);
    }

    public void reset() {
        isDraggableRelay.accept(isInitiallyDraggable);
    }
}
