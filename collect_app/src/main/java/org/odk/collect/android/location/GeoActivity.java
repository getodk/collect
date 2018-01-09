package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    @BindView(R.id.save_button)
    protected ImageButton saveButton;

    private final BehaviorRelay<GoogleMap> mapRelay =
            BehaviorRelay.create();

    private final Observable<GoogleMap> observeMap = mapRelay.hide();

    private final BehaviorRelay<Optional<Marker>> markerRelay =
            BehaviorRelay.createDefault(Optional.absent());

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
        viewModel.locationInfoVisibility()
                .compose(bindToLifecycle())
                .subscribe(locationInfoText::setVisibility, Timber::e);

        viewModel.locationInfoText()
                .compose(bindToLifecycle())
                .subscribe(locationInfoText::setText, Timber::e);

        // Bind Location Status:
        viewModel.locationStatusVisibility()
                .compose(bindToLifecycle())
                .subscribe(locationStatusText::setVisibility, Timber::e);

        viewModel.locationStatusText()
                .compose(bindToLifecycle())
                .subscribe(locationStatusText::setText, Timber::e);

        // Bind Button Visibility:
        viewModel.pauseButtonVisibility()
                .compose(bindToLifecycle())
                .subscribe(pauseButton::setVisibility, Timber::e);

        // Bind Button Enable Status:
        viewModel.isAddLocationEnabled()
                .compose(bindToLifecycle())
                .subscribe(addButton::setEnabled);

        viewModel.isShowLocationEnabled()
                .compose(bindToLifecycle())
                .subscribe(showButton::setEnabled);

        // Bind Button events:
        viewModel.observeLocationCleared()
                .compose(bindToLifecycle())
                .subscribe(__ -> clearMarker(), Timber::e);

        // Bind Dialog events:
        viewModel.shouldShowZoomDialog()
                .compose(bindToLifecycle())
                .subscribe(zoomDialog::show, Timber::e);

        viewModel.shouldShowGpsAlert()
                .compose(bindToLifecycle())
                .subscribe(showGpsDisabledAlert::show, Timber::e);

        viewModel.shouldShowLayers()
                .withLatestFrom(observeMap, Rx::takeRight)
                .compose(bindToLifecycle())
                .subscribe(this::shouldShowLayers, Timber::e);

        // Bind location Marks:
        viewModel.observeMarkedLocation()
                .compose(bindToLifecycle())
                .subscribe(this::updateMarkerForPosition, Timber::e);

        // Bind Map Zoom:
        zoomDialog.zoomToLocation()
                .map(latLng -> CameraUpdateFactory.newLatLngZoom(latLng, 16))
                .compose(bindToLifecycle())
                .subscribe(this::updateCamera, Timber::e);

        observeMarker.filter(Optional::isPresent)
                .map(Optional::get)
                .map(Marker::getPosition)
                .compose(bindToLifecycle())
                .subscribe(viewModel::markLocation, Timber::e);

        observeMarker.filter(it -> !it.isPresent())
                .compose(bindToLifecycle())
                .subscribe(__ -> viewModel.clearMarkedLocation(), Timber::e);
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
                .subscribe(Rx::noop, Timber::e);;
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
                .subscribe(saveAnswer::save, Timber::e, this::finish);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        getViewModel().markLocation(latLng);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        markerRelay.accept(Optional.of(marker));
    }

    private void shouldShowLayers(@NonNull GoogleMap googleMap) {
        new MapHelper(this, googleMap)
                .showLayersDialog(this);
    }

    private void updateCamera(@NonNull CameraUpdate cameraUpdate) {
        observeMap.firstOrError()
                .compose(bindToLifecycle())
                .subscribe(googleMap -> googleMap.animateCamera(cameraUpdate), Timber::e);
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
                            .map(Optional::of);

                }).subscribe(markerRelay, Timber::e);
    }

    private void clearMarker() {
        observeMarker.firstOrError()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .compose(bindToLifecycle())
                .subscribe(marker -> {
                    marker.remove();
                    markerRelay.accept(Optional.absent());

                }, Timber::e);
    }

}
