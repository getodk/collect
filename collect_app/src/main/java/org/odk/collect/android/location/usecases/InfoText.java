package org.odk.collect.android.location.usecases;


import android.content.Context;
import android.support.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerViewModel
public class InfoText {

    @NonNull
    private final Context context;

    @NonNull
    private final IsDraggable isDraggable;

    @Inject
    InfoText(@NonNull Context context,
             @NonNull IsDraggable isDraggable) {
        this.context = context;
        this.isDraggable = isDraggable;
    }

    public Observable<String> observe() {
        return isDraggable.observe()
                .map(isDraggable -> isDraggable
                        ? R.string.geopoint_instruction
                        : R.string.geopoint_no_draggable_instruction)
                .map(context::getString);
    }
}
