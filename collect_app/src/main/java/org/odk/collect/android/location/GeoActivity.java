package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.odk.collect.android.R;
import org.odk.collect.android.architecture.rx.RxMVVMActivity;
import org.odk.collect.android.location.usecases.CurrentMap;
import org.odk.collect.android.location.usecases.GetMap;
import org.odk.collect.android.location.usecases.OnMapError;
import org.odk.collect.android.location.usecases.SaveAnswer;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class GeoActivity extends RxMVVMActivity<GeoViewModel> implements GoogleMap.OnMapLongClickListener {

    @Inject
    protected GetMap getMap;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getMap.get()
                .compose(bindToLifecycle())
                .subscribe(this::onMapReady,
                        onMapError::onError);

        getViewModel().isPauseVisible()
                .compose(bindToLifecycle())
                .map(isVisible -> isVisible ? View.VISIBLE : View.GONE)
                .subscribe(pauseButton::setVisibility);

        getViewModel().isReloadEnabled()
                .compose(bindToLifecycle())
                .subscribe(addButton::setEnabled);

        getViewModel().isShowEnabled()
                .compose(bindToLifecycle())
                .subscribe(showButton::setEnabled);
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
        getViewModel().addLocation()
                .compose(bindToLifecycle())
                .doOnError(Timber::e)
                .subscribe();
    }

    @OnClick(R.id.pause_button)
    protected void onPauseClick() {
        getViewModel().pause()
                .compose(bindToLifecycle())
                .doOnError(Timber::e)
                .subscribe();
    }

    @OnClick(R.id.show_button)
    protected void onShowClick() {
        getViewModel().showLocation()
                .compose(bindToLifecycle())
                .doOnError(Timber::e)
                .subscribe();
    }

    @OnClick(R.id.layers_button)
    protected void onLayersClick() {
        getViewModel().showLayers()
                .compose(bindToLifecycle())
                .doOnError(Timber::e)
                .subscribe();
    }

    @OnClick(R.id.clear_button)
    protected void onClearClick() {
        getViewModel().clearLocation()
                .compose(bindToLifecycle())
                .doOnError(Timber::e)
                .subscribe();
    }

    @OnClick(R.id.save_button)
    protected void onSaveClick() {
        getViewModel().saveLocation()
                .compose(bindToLifecycle())
                .subscribe(saveAnswer::save, Timber::e, this::finish);
    }

    private void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setOnMapLongClickListener(this);
        currentMap.update(googleMap);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
//        this.latLng = latLng;
//        if (marker == null) {
//            markerOptions.position(latLng);
//            marker = map.addMarker(markerOptions);
//        } else {
//            marker.setPosition(latLng);
//        }
//        enableShowLocation(true);
//        marker.setDraggable(true);
//        isDragged = true;
//        setClear = false;
//        captureLocation = true;
    }
}
