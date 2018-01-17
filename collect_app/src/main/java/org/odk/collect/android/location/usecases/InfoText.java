package org.odk.collect.android.location.usecases;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerApplication
public class InfoText {

    @NonNull
    private final Observable<String> observeText;

    @NonNull
    private final Observable<Integer> observeVisibility;

    @Inject
    InfoText(@NonNull Context context,
             @NonNull IsDraggable isDraggable,
             @NonNull InitialLocation initialLocation,
             @NonNull WasCleared wasCleared) {

        observeText = isDraggable.observe()
                .map(itsDraggable -> itsDraggable
                        ? R.string.geopoint_instruction
                        : R.string.geopoint_no_draggable_instruction)
                .map(context::getString);

        observeVisibility = Observable.combineLatest(
                initialLocation.observePresence(),
                wasCleared.observe(),
                (hasInitial, hasBeenCleared) ->
                        hasInitial && !hasBeenCleared)
                .map(shouldHide -> shouldHide ? View.GONE : View.VISIBLE);
    }

    @NonNull
    public Observable<String> observeText() {
        return observeText;
    }

    @NonNull
    public Observable<Integer> observeVisibilty() {
        return observeVisibility;
    }
}
