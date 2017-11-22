package org.odk.collect.android.location;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Window;

import com.google.android.gms.maps.GoogleMap;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.ViewModelActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.databinding.ActivityGeoBinding;
import org.odk.collect.android.location.domain.GetMap;
import org.odk.collect.android.location.domain.SetupMap;
import org.odk.collect.android.injection.CollectViewModelFactory;
import org.odk.collect.android.utilities.ToastUtils;

import javax.inject.Inject;

import timber.log.Timber;

public class GeoActivity
        extends ViewModelActivity<GeoViewModel, CollectViewModelFactory, ActivityGeoBinding> {

    @Inject
    public GetMap getMap;

    @Inject
    public SetupMap setupMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        addSubscription(getMap.get()
                .flatMap(setupMap::setup)
                .subscribe(this::setMap, this::onMapError));

        addSubscription(getViewModel().observeAcceptLocation()
                .subscribe(__ -> onAcceptLocation(), this::onUnknownError));
    }

    private void setMap(@NonNull GoogleMap map) {
        addSubscription(getViewModel().observeReloadMap(map)
                .subscribe(__ -> onMapReload(), this::onMapError));
    }


    private void onMapError(@NonNull Throwable throwable) {
        if (throwable instanceof IllegalStateException) {
            ToastUtils.showShortToast(R.string.google_play_services_error_occured);
            finish();
            return;
        }

        onUnknownError(throwable);
    }

    private void onMapReload() {
        Timber.i("Map was reloaded.");
    }

    private void onUnknownError(@NonNull Throwable throwable) {
        Timber.e("An unknown error occurred!", throwable);
    }

    private void onAcceptLocation() {

        Collect.getInstance().getActivityLogger().logInstanceAction(this, "acceptLocation",
                "OK");

        Intent i = new Intent();
//        if (setClear || (readOnly && latLng == null)) {
//            i.putExtra(FormEntryActivity.LOCATION_RESULT, "");
//
//        } else if (isDragged || readOnly || locationFromIntent) {
//            Timber.i("IsDragged !!!");
//            i.putExtra(
//                    FormEntryActivity.LOCATION_RESULT,
//                    latLng.latitude + " " + latLng.longitude + " "
//                            + 0 + " " + 0);
//        } else if (location != null) {
//            Timber.i("IsNotDragged !!!");
//
//            i.putExtra(
//                    FormEntryActivity.LOCATION_RESULT,
//                    getResultString(location)
//            );
//        }

        setResult(RESULT_OK, i);
        finish();
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
