package org.odk.collect.android.location.domain.viewstate;


import android.support.annotation.NonNull;

import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.domain.state.SelectedLocation;
import org.odk.collect.android.location.domain.state.ShowZoomOnInitialLocation;
import org.odk.collect.android.location.domain.state.CurrentLocation;
import org.odk.collect.android.location.model.ZoomData;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

@PerActivity
public class OnShowZoomDialog {

    @NonNull
    private final ShowZoomOnInitialLocation showZoomOnInitialLocation;

    @NonNull
    private final CurrentLocation currentLocation;

    @NonNull
    private final SelectedLocation selectedLocation;

    private final PublishRelay<Object> showLocationRelay = PublishRelay.create();

    @Inject
    OnShowZoomDialog(@NonNull ShowZoomOnInitialLocation showZoomOnInitialLocation,
                     @NonNull CurrentLocation currentLocation,
                     @NonNull SelectedLocation selectedLocation) {

        this.showZoomOnInitialLocation = showZoomOnInitialLocation;
        this.currentLocation = currentLocation;
        this.selectedLocation = selectedLocation;
    }

    public Observable<ZoomData> observe() {
        return Observable.merge(showLocationRelay.hide(), showZoomOnInitialLocation.observe())
                .flatMapSingle(__ -> Single.zip(
                        currentLocation.get(),
                        selectedLocation.get(),
                        (current, selected) -> new ZoomData(current.orNull(), selected.orNull())
                ))
                .filter(zoomData -> !zoomData.isEmpty());
    }

    public Completable show() {
        return Completable.defer(() -> {
            showLocationRelay.accept(this);
            return Completable.complete();
        });
    }
}
