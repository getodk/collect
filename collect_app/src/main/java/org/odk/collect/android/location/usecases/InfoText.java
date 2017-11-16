package org.odk.collect.android.location.usecases;


import android.content.Context;
import android.support.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerApplication;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;

@PerApplication
public class InfoText extends Observable<String> {

    @NonNull
    private final Observable<String> infoText;

    @Inject
    InfoText(@NonNull Context context,
             @NonNull InitialState initialState) {
        this.infoText = initialState.isDraggable()
                .map(itsDraggable -> itsDraggable
                        ? R.string.geopoint_instruction
                        : R.string.geopoint_no_draggable_instruction)
                .map(context::getString);
    }

    @Override
    protected void subscribeActual(Observer<? super String> observer) {
        infoText.subscribe(observer);
    }
}
