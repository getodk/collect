package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.ImageButton;

import org.odk.collect.android.R;
import org.odk.collect.android.architecture.rx.RxMVVMActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class GeoActivity extends RxMVVMActivity<GeoViewModel> {

    @BindView(R.id.add_button)
    ImageButton addButton;

    @BindView(R.id.show_button)
    ImageButton showButton;

    @OnClick(R.id.add_button)
    protected void onAddClick() {
    }

    @OnClick(R.id.pause_button)
    protected void onPauseClick() {

    }

    @OnClick(R.id.show_button)
    protected void onShowClick() {

    }

    @OnClick(R.id.layers_button)
    protected void onLayersClick() {

    }

    @OnClick(R.id.clear_button)
    protected void onClearClick() {

    }

    @OnClick(R.id.save_button)
    protected void onSaveClick() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

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

}
