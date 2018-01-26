package org.odk.collect.android.location;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.architecture.rx.RxViewModelActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class GeoActivity extends RxViewModelActivity<GeoViewModel> {

    @BindView(R.id.location_info)
    protected TextView locationInfoText;

    @BindView(R.id.location_status)
    protected TextView locationStatusText;

    @BindView(R.id.map_container)
    protected FrameLayout mapContainer;

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
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_geo;
    }

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

}
