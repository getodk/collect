package org.odk.collect.android.location;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;

import org.odk.collect.android.architecture.rx.RxViewModel;
import org.odk.collect.android.location.domain.actions.AddLocation;
import org.odk.collect.android.location.domain.actions.ClearLocation;
import org.odk.collect.android.location.domain.actions.SaveAnswer;
import org.odk.collect.android.location.domain.state.CurrentLocation;
import org.odk.collect.android.location.domain.state.SelectedLocation;
import org.odk.collect.android.location.domain.viewstate.InfoText;
import org.odk.collect.android.location.domain.viewstate.IsAddEnabled;
import org.odk.collect.android.location.domain.viewstate.IsClearEnabled;
import org.odk.collect.android.location.domain.viewstate.IsShowEnabled;
import org.odk.collect.android.location.domain.viewstate.OnShowZoomDialog;
import org.odk.collect.android.location.domain.viewstate.OnZoom;
import org.odk.collect.android.location.domain.viewstate.PauseVisibility;
import org.odk.collect.android.location.domain.viewstate.StatusText;
import org.odk.collect.android.location.model.ZoomData;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;


public class GeoViewModel
        extends RxViewModel
        implements GeoViewModelType {

    // Internal State:
    @NonNull
    private final CurrentLocation currentLocation;

    @NonNull
    private final SelectedLocation selectedLocation;

    // View outputs:
    @NonNull
    private final InfoText infoText;

    @NonNull
    private final StatusText statusText;

    @NonNull
    private final PauseVisibility pauseVisibility;

    @NonNull
    private final IsAddEnabled isAddEnabled;

    @NonNull
    private final IsShowEnabled isShowEnabled;

    @NonNull
    private final IsClearEnabled isClearEnabled;

    @NonNull
    private final OnZoom onZoom;

    @NonNull
    private final OnShowZoomDialog onShowZoomDialog;

    // View inputs:
    @NonNull
    private final AddLocation addLocation;

    @NonNull
    private final ClearLocation clearLocation;

    @NonNull
    private final SaveAnswer saveAnswer;

    @Inject
    GeoViewModel(@NonNull InfoText infoText,
                 @NonNull StatusText statusText,
                 @NonNull PauseVisibility pauseVisibility,
                 @NonNull IsAddEnabled isAddEnabled,
                 @NonNull IsShowEnabled isShowEnabled,
                 @NonNull IsClearEnabled isClearEnabled,
                 @NonNull AddLocation addLocation,
                 @NonNull ClearLocation clearLocation,
                 @NonNull SaveAnswer saveAnswer,
                 @NonNull OnZoom onZoom,
                 @NonNull CurrentLocation currentLocation,
                 @NonNull SelectedLocation selectedLocation,
                 @NonNull OnShowZoomDialog onShowZoomDialog) {

        this.infoText = infoText;
        this.statusText = statusText;
        this.pauseVisibility = pauseVisibility;
        this.isAddEnabled = isAddEnabled;
        this.isShowEnabled = isShowEnabled;
        this.isClearEnabled = isClearEnabled;
        this.addLocation = addLocation;
        this.clearLocation = clearLocation;
        this.saveAnswer = saveAnswer;
        this.onZoom = onZoom;
        this.currentLocation = currentLocation;
        this.selectedLocation = selectedLocation;
        this.onShowZoomDialog = onShowZoomDialog;
    }

    // UI state:
    @NonNull
    @Override
    public Observable<String> locationInfoText() {
        return infoText.observeText();
    }

    @NonNull
    @Override
    public Observable<Integer> locationInfoVisibility() {
        return infoText.observeVisibility();
    }

    @NonNull
    @Override
    public Observable<String> locationStatusText() {
        return statusText.observeText();
    }

    @NonNull
    @Override
    public Observable<Integer> locationStatusVisibility() {
        return statusText.observeVisibility();
    }

    @NonNull
    @Override
    public Observable<Integer> pauseButtonVisibility() {
        return pauseVisibility.observe();
    }

    @NonNull
    @Override
    public Observable<Boolean> isAddLocationEnabled() {
        return isAddEnabled.observe();
    }

    @NonNull
    @Override
    public Observable<Boolean> isShowLocationEnabled() {
        return isShowEnabled.observe();
    }

    @NonNull
    @Override
    public Observable<Boolean> isClearLocationEnabled() {
        return isClearEnabled.observe();
    }

    @NonNull
    @Override
    public Observable<Optional<LatLng>> selectedLocation() {
        return selectedLocation.observe();
    }

    @NonNull
    @Override
    public Observable<ZoomData> onShowZoomDialog() {
        return onShowZoomDialog.observe();
    }

    @NonNull
    @Override
    public Observable<Object> onShowGpsDisabledDialog() {
        return currentLocation.onError();
    }

    @NonNull
    @Override
    public Observable<LatLng> onZoom() {
        return onZoom.observe();
    }

    @NonNull
    @Override
    public Completable addLocation() {
        return addLocation.add();
    }

    @NonNull
    @Override
    public Completable pause() {
        return Completable.complete();
    }

    @NonNull
    @Override
    public Completable showLocation() {
        return onShowZoomDialog.show();
    }

    @NonNull
    @Override
    public Completable clearLocation() {
        return clearLocation.clear();
    }

    @NonNull
    @Override
    public Completable saveLocation() {
        return saveAnswer.save();
    }

    @NonNull
    @Override
    public Completable mapLongPressed(@NonNull LatLng latLng) {
        return selectedLocation.select(latLng);
    }

    @NonNull
    @Override
    public Completable markerMoved(@NonNull LatLng latLng) {
        return selectedLocation.select(latLng);
    }

    @Override
    public void startLocation() {
        currentLocation.startLocation();
    }

    @Override
    public void stopLocation() {
        currentLocation.stopLocation();
    }
}
