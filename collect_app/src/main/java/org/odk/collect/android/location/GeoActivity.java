package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.R;
import org.odk.collect.android.architecture.rx.RxMVVMActivity;
import org.odk.collect.android.location.usecases.LoadMap;
import org.odk.collect.android.location.usecases.OnMapError;
import org.odk.collect.android.location.usecases.SaveAnswer;
import org.odk.collect.android.location.usecases.ShowGpsDisabledAlert;
import org.odk.collect.android.location.usecases.ZoomDialog;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

public class GeoActivity
        extends RxMVVMActivity<GeoViewModel>
        implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

    // Use cases:
    @Inject
    protected LoadMap loadMap;

    @Inject
    protected SaveAnswer saveAnswer;

    @Inject
    protected OnMapError onMapError;

    @Inject
    protected ZoomDialog zoomDialog;

    @Inject
    protected ShowGpsDisabledAlert showGpsDisabledAlert;

    // Outputs:
    @BindView(R.id.location_info)
    protected TextView locationInfoText;

    @BindView(R.id.location_status)
    protected TextView locationStatusText;

    @BindView(R.id.add_button)
    protected ImageButton addButton;

    @BindView(R.id.show_button)
    protected ImageButton showButton;

    @BindView(R.id.pause_button)
    protected ImageButton pauseButton;

    @BindView(R.id.clear_button)
    protected ImageButton clearButton;

    @BindView(R.id.save_button)
    protected ImageButton saveButton;

    @NonNull
    private final BehaviorRelay<GoogleMap> mapRelay =
            BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<LatLng> zoomRelay = BehaviorRelay.create();

    @NonNull
    private final Observable<LatLng> observeZoom = zoomRelay.hide();

    @NonNull
    private final Observable<GoogleMap> observeMap = mapRelay.hide();

    @NonNull
    private final BehaviorRelay<Optional<Marker>> markerRelay =
            BehaviorRelay.createDefault(Optional.absent());

    @NonNull
    private final Observable<Optional<Marker>> observeMarker = markerRelay.hide();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        // Load Map:
        loadMap.load()
                .compose(bindToLifecycle())
                .subscribe(mapRelay, onMapError::onError);

        GeoViewModel viewModel = getViewModel();

        // Bind Location Info:
        viewModel.observeLocationInfoVisibility()
                .compose(bindToLifecycle())
                .subscribe(locationInfoText::setVisibility, Timber::e);

        viewModel.observeLocationInfoText()
                .compose(bindToLifecycle())
                .subscribe(locationInfoText::setText, Timber::e);

        // Bind Location Status:
        viewModel.observeLocationStatusVisibility()
                .compose(bindToLifecycle())
                .subscribe(locationStatusText::setVisibility, Timber::e);

        viewModel.observeLocationStatusText()
                .compose(bindToLifecycle())
                .subscribe(locationStatusText::setText, Timber::e);

        // Bind Button Visibility:
        viewModel.observePauseButtonVisibility()
                .compose(bindToLifecycle())
                .subscribe(pauseButton::setVisibility, Timber::e);

        // Bind Button Enable Status:
        viewModel.observeAddLocationEnabled()
                .compose(bindToLifecycle())
                .subscribe(addButton::setEnabled);

        viewModel.observeShowLocationEnabled()
                .compose(bindToLifecycle())
                .subscribe(showButton::setEnabled);

        viewModel.observeClearLocationEnabled()
                .compose(bindToLifecycle())
                .subscribe(clearButton::setEnabled);

        // Bind Button events:
        viewModel.observeLocationCleared()
                .compose(bindToLifecycle())
                .subscribe(__ -> clearMarker(), Timber::e);

        // Bind Dialog events:
        viewModel.observeShowZoomDialog()
                .compose(bindToLifecycle())
                .subscribe(zoomDialog::show, Timber::e);

        viewModel.observeShowGpsAlert()
                .compose(bindToLifecycle())
                .subscribe(showGpsDisabledAlert::show, Timber::e);

        viewModel.observeShowLayers()
                .withLatestFrom(observeMap, Rx::takeRight)
                .compose(bindToLifecycle())
                .subscribe(this::shouldShowLayers, Timber::e);

        // Bind location Marks:
        viewModel.observeMarkedLocation()
                .compose(bindToLifecycle())
                .subscribe(this::updateMarkerForPosition, Timber::e);

        viewModel.observeInitialLocation()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .compose(bindToLifecycle())
                .subscribe(zoomRelay, Timber::e);

        // Bind Map Zoom:
        zoomDialog.zoomToLocation()
                .compose(bindToLifecycle())
                .subscribe(zoomRelay, Timber::e);

        observeMarker.filter(Optional::isPresent)
                .map(Optional::get)
                .map(Marker::getPosition)
                .flatMapCompletable(viewModel::selectLocation)
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);

        observeMarker.filter(it -> !it.isPresent())
                .skip(1)    // Hack to get around the circular marker bug.
                .flatMapCompletable(__ -> viewModel.clearSelectedLocation())
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);

        // CameraUpdateFactory won't be initialized until we have a map, so we have to make sure we
        // have the map first:
        Observable.combineLatest(observeZoom, observeMap, (latLng, map) -> {
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f);
            map.animateCamera(update);

            return new Pair<>(update, map);

        }).subscribe(this::updateCamera, Timber::e);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getViewModel().startWatchingLocation();
    }

    @Override
    protected void onStop() {
        getViewModel().stopWatchingLocation();
        super.onStop();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_geo;
    }

    @NonNull
    @Override
    protected Class<GeoViewModel> getViewModelClass() {
        return GeoViewModel.class;
    }

    // Inputs:

    @OnClick(R.id.add_button)
    protected void onAddClick() {
        getViewModel().addLocation()
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @OnClick(R.id.pause_button)
    protected void onPauseClick() {
        getViewModel().pause()
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @OnClick(R.id.show_button)
    protected void onShowClick() {
        getViewModel().showLocation()
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @OnClick(R.id.layers_button)
    protected void onLayersClick() {
        getViewModel().showLayers()
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @OnClick(R.id.clear_button)
    protected void onClearClick() {
        getViewModel().clearLocation()
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @OnClick(R.id.save_button)
    protected void onSaveClick() {
        getViewModel().saveLocation()
                .compose(bindToLifecycle())
                .subscribe(saveAnswer::save, Timber::e);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        getViewModel().markLocation(latLng)
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        markerRelay.accept(Optional.of(marker));
    }

    private void shouldShowLayers(@NonNull GoogleMap googleMap) {
        new MapHelper(this, googleMap)
                .showLayersDialog(this);
    }

    private void updateCamera(@NonNull Pair<CameraUpdate, GoogleMap> updatePair) {
        CameraUpdate cameraUpdate = updatePair.first;
        GoogleMap googleMap = updatePair.second;

        if (cameraUpdate == null || googleMap == null){
            Timber.e("Can't update without both the CameraUpdate and the GoogleMap.");
            return;
        }

        googleMap.animateCamera(cameraUpdate);
    }

    private void updateMarkerForPosition(@Nullable LatLng position) {
        if (position == null) {
            clearMarker();
            return;
        }

        observeMarker.firstOrError()
                .flatMap(markerOptional -> {

                    if (markerOptional.isPresent()) {
                        markerOptional.get().setPosition(position);

                        return Single.just(markerOptional);
                    }

                    return observeMap.firstOrError()
                            .map(googleMap -> googleMap.addMarker(new MarkerOptions().position(position)))
                            .zipWith(getViewModel().observeIsDraggable().firstOrError(), (marker, isDraggable) -> {
                                marker.setDraggable(isDraggable);
                                return marker;
                            })
                            .map(Optional::of);

                }).subscribe(markerRelay, Timber::e);
    }

    private void clearMarker() {
        observeMarker.firstOrError()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .compose(bindToLifecycle())
                .doOnSuccess(Marker::remove)
                .map(marker -> Optional.<Marker>absent())
                .subscribe(markerRelay, Timber::e);
    }

    //region Unused interface methods.
    @Override
    public void onMarkerDragStart(Marker marker) {
        // Do nothing.
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        // Do nothing.
    }
    //endregion
}
