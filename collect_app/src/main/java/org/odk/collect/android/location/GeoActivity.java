package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.ViewModelActivity;

public class GeoActivity extends ViewModelActivity<GeoViewModel> {
    @Override
    protected void onCreate(@android.support.annotation.Nullable Bundle savedInstanceState) {
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
