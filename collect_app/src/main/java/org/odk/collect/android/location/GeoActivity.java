package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.R;
import org.odk.collect.android.architecture.rx.RxMVVMActivity;
import org.odk.collect.android.location.usecases.CurrentMap;
import org.odk.collect.android.location.usecases.LoadMap;
import org.odk.collect.android.location.usecases.OnMapError;
import org.odk.collect.android.location.usecases.SaveAnswer;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import timber.log.Timber;

public class GeoActivity
        extends RxMVVMActivity<GeoViewModel>
        implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

    @Inject
    protected LoadMap loadMap;

    @Inject
    protected CurrentMap currentMap;

    @Inject
    protected SaveAnswer saveAnswer;

    @Inject
    protected OnMapError onMapError;

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

    private final BehaviorRelay<Optional<Marker>> markerRelay =
            BehaviorRelay.createDefault(Optional.absent());

    private final Observable<Optional<Marker>> marker = markerRelay.hide();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        loadMap.load()
                .compose(bindToLifecycle())
                .subscribe(this::onMapReady, onMapError::onError);

        getViewModel().hasInitialLocation()
                .compose(bindToLifecycle())
                .map(hasInitialLocation -> hasInitialLocation ? View.GONE : View.VISIBLE)
                .subscribe(visibility -> {
                    locationInfoText.setVisibility(visibility);
                    locationStatusText.setVisibility(visibility);

                }, Timber::e);


//        getViewModel().shouldZoomToLatLng()
//                .map(latLng -> CameraUpdateFactory.newLatLngZoom(latLng, 16))
//                .subscribe(this::updateCamera, Timber::e);

        getViewModel().observeMarkerOptions()
                .withLatestFrom(marker, (markerOptions, marker) -> {
                    if (marker.isPresent()) {
                        marker.get().remove();
                    }

                    return markerOptions;

                }).withLatestFrom(currentMap.observe(), (markerOptionsOptional, googleMap) -> {
                    Marker marker = null;

                    if (markerOptionsOptional.isPresent()) {
                        MarkerOptions markerOptions = markerOptionsOptional.get();
                        marker = googleMap.addMarker(markerOptions);
                    }

                    return Optional.fromNullable(marker);

                })
                .withLatestFrom(isMarkerDraggable(), (marker, isMarkerDraggable) -> {
                    if (marker.isPresent()) {
                        marker.get().setDraggable(isMarkerDraggable);
                    }

                    return marker;
                })
                .subscribe(markerRelay);
    }

    private Observable<Boolean> isMarkerDraggable() {
        return Observable.combineLatest(getViewModel().isDraggable(), getViewModel().isReadOnly(),
                (isDraggable, isReadOnly) -> isDraggable && !isReadOnly);
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
        getViewModel().setMarkedLocation(latLng);
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

    private void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setOnMapLongClickListener(this);
        currentMap.update(googleMap);
    }

    private void updateCamera(@NonNull CameraUpdate cameraUpdate) {
        currentMap.get()
                .compose(bindToLifecycle())
                .subscribe(googleMap -> googleMap.animateCamera(cameraUpdate),
                        Timber::e);
    }

}
