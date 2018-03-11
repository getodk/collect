package org.odk.collect.android.location.domain.viewstate;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.domain.state.SelectedLocation;
import org.odk.collect.android.location.injection.Qualifiers.HasInitialLocation;
import org.odk.collect.android.location.injection.Qualifiers.IsDraggable;
import org.odk.collect.android.location.injection.Qualifiers.IsReadOnly;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerActivity
public class InfoText {
    @NonNull
    private final Context context;

    @NonNull
    private final SelectedLocation selectedLocation;

    private final boolean isDraggable;
    private final boolean isReadOnly;
    private final boolean hasInitialLocation;

    @Inject
    InfoText(@NonNull Context context,
             @NonNull SelectedLocation selectedLocation,
             @IsDraggable boolean isDraggable,
             @IsReadOnly boolean isReadOnly,
             @HasInitialLocation boolean hasInitialLocation) {
        this.context = context;
        this.selectedLocation = selectedLocation;
        this.isDraggable = isDraggable;
        this.isReadOnly = isReadOnly;
        this.hasInitialLocation = hasInitialLocation;
    }

    public Observable<String> observeText() {
        return Observable.just(isDraggable
                ? R.string.geopoint_instruction
                : R.string.geopoint_no_draggable_instruction

        ).map(context::getString);
    }

    public Observable<Integer> observeVisibility() {
        return selectedLocation.hasBeenCleared()
                .map(this::shouldHide);
    }

    private int shouldHide(boolean wasCleared) {
        return isReadOnly || (hasInitialLocation && !wasCleared)
                ? View.GONE
                : View.VISIBLE;
    }
}
