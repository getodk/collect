package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;

import org.odk.collect.android.R;
import org.odk.collect.android.architecture.rx.RxMvvmActivity;
import org.odk.collect.android.location.mapviewmodel.MapViewModel;
import org.odk.collect.android.location.activity.OnMapError;
import org.odk.collect.android.location.activity.SaveAnswer;
import org.odk.collect.android.location.activity.ShowGpsDisabledAlert;
import org.odk.collect.android.location.activity.ZoomDialog;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import timber.log.Timber;

public class GeoActivity
        extends RxMvvmActivity<GeoViewModel> {

    // Use cases:
    @Inject
    protected SaveAnswer saveAnswer;

    @Inject
    protected OnMapError onMapError;

    @Inject
    protected ZoomDialog zoomDialog;

    @Inject
    protected ShowGpsDisabledAlert showGpsDisabledAlert;

    @Inject
    protected MapViewModel mapViewModel;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        GeoViewModel viewModel = getViewModel();

        // Bind VM Initial State:
        viewModel.isDraggable()
                .flatMapCompletable(mapViewModel::updateIsDraggable)
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);

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

        viewModel.isClearLocationEnabled()
                .compose(bindToLifecycle())
                .subscribe(clearButton::setEnabled);

        // Bind Button events:
        viewModel.onLocationAdded()
                .flatMapCompletable(mapViewModel::markLocation)
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);

        viewModel.onLocationCleared()
                .flatMapCompletable(__ -> mapViewModel.clearMarkedLocation())
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);

        // Bind Dialog events:
        viewModel.onShowZoomDialog()
                .compose(bindToLifecycle())
                .subscribe(zoomDialog::show, Timber::e);

        viewModel.onShowGpsAlert()
                .compose(bindToLifecycle())
                .subscribe(showGpsDisabledAlert::show, Timber::e);

        viewModel.onShowLayers()
                .compose(bindToLifecycle())
                .flatMapCompletable(__ -> mapViewModel.showLayers())
                .subscribe(Rx::noop, Timber::e);

        // Bind MapViewModel:

        mapViewModel.loadMap()
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);

        mapViewModel.observeMarkedLocation()
                .flatMapCompletable(viewModel::selectLocation)
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);

        mapViewModel.observeClearedLocation()
                .flatMapCompletable(__ -> viewModel.clearSelectedLocation())
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);

        Observable.merge(viewModel.onInitialLocation(), zoomDialog.zoomToLocation())
                .flatMapCompletable(mapViewModel::zoomToLocation)
                .compose(bindToLifecycle())
                .subscribe(Rx::noop, Timber::e);
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

    // MvvmActivity:

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
}
