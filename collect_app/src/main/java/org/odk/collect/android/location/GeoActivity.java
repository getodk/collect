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

    private final BehaviorRelay<GoogleMap> currentMap =
            BehaviorRelay.create();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        loadMap.load()
                .compose(bindToLifecycle())
                .subscribe(currentMap, onMapError::onError);

        getViewModel().locationInfoVisibility()
                .compose(bindToLifecycle())
                .subscribe(locationInfoText::setVisibility, Timber::e);

        getViewModel().locationStatusVisibility()
                .compose(bindToLifecycle())
                .subscribe(locationStatusText::setVisibility, Timber::e);

        getViewModel().locationInfoText()
                .compose(bindToLifecycle())
                .subscribe(locationInfoText::setText, Timber::e);

        getViewModel().locationStatusText()
                .compose(bindToLifecycle())
                .subscribe(locationStatusText::setText, Timber::e);

        getViewModel().pauseButtonVisibility()
                .compose(bindToLifecycle())
                .subscribe(pauseButton::setVisibility, Timber::e);

        getViewModel().isAddLocationEnabled()
                .compose(bindToLifecycle())
                .subscribe(addButton::setEnabled);

        getViewModel().isShowLocationEnabled()
                .compose(bindToLifecycle())
                .subscribe(showButton::setEnabled);

        getViewModel().shouldShowLayers()
                .withLatestFrom(currentMap, Rx::takeRight)
                .compose(bindToLifecycle())
                .subscribe(this::shouldShowLayers, Timber::e);

        getViewModel().shouldShowZoomDialog()
                .compose(bindToLifecycle())
                .subscribe(zoomDialog::show, Timber::e);

        getViewModel().shouldShowGpsAlert()
                .compose(bindToLifecycle())
                .subscribe(showGpsDisabledAlert::show, Timber::e);

        zoomDialog.zoomToLocation()
                .map(latLng -> CameraUpdateFactory.newLatLngZoom(latLng, 16))
                .compose(bindToLifecycle())
                .subscribe(this::updateCamera, Timber::e);
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
        getViewModel().addLocation();
    }

    @OnClick(R.id.pause_button)
    protected void onPauseClick() {
        getViewModel().pause();
    }

    @OnClick(R.id.show_button)
    protected void onShowClick() {
        getViewModel().showLocation();
    }

    @OnClick(R.id.layers_button)
    protected void onLayersClick() {
        getViewModel().showLayers();
    }

    @OnClick(R.id.clear_button)
    protected void onClearClick() {
        getViewModel().clearLocation();
    }

    @OnClick(R.id.save_button)
    protected void onSaveClick() {
        getViewModel().saveLocation()
                .compose(bindToLifecycle())
                .subscribe(saveAnswer::save, Timber::e, this::finish);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    private void shouldShowLayers(@NonNull GoogleMap googleMap) {
        new MapHelper(this, googleMap)
                .showLayersDialog(this);
    }

    private void updateCamera(@NonNull CameraUpdate cameraUpdate) {

        currentMap.hide()
                .compose(bindToLifecycle())
                .subscribe(googleMap -> googleMap.animateCamera(cameraUpdate), Timber::e);
    }
}
