package org.odk.collect.android.location.domain.state;

import android.support.annotation.NonNull;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.injection.Qualifiers.HasInitialLocation;
import org.odk.collect.android.location.injection.Qualifiers.IsDraggable;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerActivity
public class ShowZoomOnInitialLocation {

    @NonNull
    private final SelectedLocation selectedLocation;

    private final boolean hasInitialLocation;
    private final boolean isDraggable;

    @Inject
    ShowZoomOnInitialLocation(@NonNull SelectedLocation selectedLocation,
                              @HasInitialLocation boolean hasInitialLocation,
                              @IsDraggable boolean isDraggable) {
        this.selectedLocation = selectedLocation;
        this.hasInitialLocation = hasInitialLocation;
        this.isDraggable = isDraggable;
    }

    public Observable<Object> observe() {
        if (hasInitialLocation || isDraggable) {
            return Observable.empty();
        }

        return selectedLocation.observePresence()
                .filter(Rx::isTrue)
                .distinctUntilChanged()
                .map(Rx::toEvent);
    }
}
