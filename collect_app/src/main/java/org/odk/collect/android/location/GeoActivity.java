package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.ViewModelActivity;
import org.odk.collect.android.databinding.ActivityGeoBinding;
import org.odk.collect.android.location.injection.GeoViewModelFactory;

import dagger.android.AndroidInjection;

public class GeoActivity extends ViewModelActivity<GeoViewModel, GeoViewModelFactory, ActivityGeoBinding> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
