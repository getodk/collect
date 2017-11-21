package org.odk.collect.android.location;

import android.support.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.ViewModelActivity;
import org.odk.collect.android.databinding.ActivityGeoBinding;
import org.odk.collect.android.location.injection.GeoViewModelFactory;

public class GeoActivity extends ViewModelActivity<GeoViewModel, GeoViewModelFactory, ActivityGeoBinding> {

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
