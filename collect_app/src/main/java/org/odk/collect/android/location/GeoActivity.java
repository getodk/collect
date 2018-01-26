package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.R;
import org.odk.collect.android.architecture.rx.RxViewModelActivity;
import org.odk.collect.android.location.domain.view.LoadMapView;
import org.odk.collect.android.location.domain.view.GpsDisabledAlert;
import org.odk.collect.android.location.domain.view.ZoomDialog;
import org.odk.collect.android.location.mapview.MapView;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.Observable;
import timber.log.Timber;

public class GeoActivity
        extends RxViewModelActivity<GeoViewModel> {

    public static final String MAP_TYPE = "map_type";
    public static final String MAP_FUNCTION = "map_function";

    private BehaviorRelay<MapView> mapViewRelay = BehaviorRelay.create();
    private Observable<MapView> observeMapView = mapViewRelay.hide();

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

    @Inject
    LoadMapView loadMapView;

    @Inject
    ZoomDialog zoomDialog;

    @Inject
    GpsDisabledAlert gpsDisabledAlert;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        loadMapView.load()
                .subscribe(mapViewRelay, Timber::e);

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
                .subscribe(addButton::setEnabled, Timber::e);

        viewModel.isShowLocationEnabled()
                .compose(bindToLifecycle())
                .subscribe(showButton::setEnabled, Timber::e);

        viewModel.isClearLocationEnabled()
                .compose(bindToLifecycle())
                .subscribe(clearButton::setEnabled, Timber::e);

        viewModel.selectedLocation()
                .flatMapCompletable(latLngOptional ->
                        markLocation(latLngOptional.orNull())
                )
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);

        viewModel.onShowZoomDialog()
                .compose(bindToLifecycle())
                .subscribe(zoomDialog::show, Timber::e);

        viewModel.onShowGpsDisabledDialog()
                .compose(bindToLifecycle())
                .subscribe(gpsDisabledAlert::show, Timber::e);

        viewModel.onZoom()
                .compose(bindToLifecycle())
                .flatMapCompletable(this::zoomToLocation)
                .subscribe(Rx::noop, Timber::e);

        observeMapView.compose(bindToLifecycle())
                .subscribe(this::bindMapView, Timber::e);

        zoomDialog.zoomToLocation()
                .flatMapCompletable(this::zoomToLocation)
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    private void bindMapView(@NonNull MapView mapView) {
        mapView.observeLongPress()
                .flatMapCompletable(viewModel::mapLongPressed)
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);

        mapView.observeMarkerMoved()
                .flatMapCompletable(viewModel::markerMoved)
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getViewModel().startLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getViewModel().stopLocation();
    }

    // Outputs:
    private Completable markLocation(@Nullable LatLng latLng) {
        return observeMapView.firstElement()
                .doOnComplete(this::onMapMissing)
                .flatMapCompletable(mapView -> mapView.markLocation(latLng));
    }

    private Completable zoomToLocation(@NonNull LatLng latLng) {
        return observeMapView.firstElement()
                .doOnComplete(this::onMapMissing)
                .flatMapCompletable(mapView -> mapView.zoomToLocation(latLng));
    }

    // Inputs:
    @OnClick(R.id.add_button)
    public void onAddClick() {
        getViewModel().addLocation()
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @OnClick(R.id.pause_button)
    public void onPauseClick() {
        getViewModel().pause()
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @OnClick(R.id.show_button)
    public void onShowClick() {
        getViewModel().showLocation()
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @OnClick(R.id.layers_button)
    public void onLayersClick() {
        observeMapView.firstElement()
                .doOnComplete(this::onMapMissing)
                .flatMapCompletable(MapView::showLayers)
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @OnClick(R.id.clear_button)
    public void onClearClick() {
        getViewModel().clearLocation()
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    @OnClick(R.id.save_button)
    public void onSaveClick() {
        getViewModel().saveLocation()
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
    }

    // ViewModelActivity:
    @Override
    protected int getLayoutId() {
        return R.layout.activity_geo;
    }

    private void onMapMissing() {
        Timber.e("No map present!");
    }
}
