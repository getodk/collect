package org.odk.collect.android.location;

import android.databinding.BindingAdapter;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.odk.collect.android.R;

import javax.annotation.Nullable;

public class BindingAdapters {
    @BindingAdapter("app:mapReadyListener")
    public static void setMapReadyListener(Fragment fragment, OnMapReadyCallback onMapReadyCallback) {
        if (fragment instanceof SupportMapFragment) {
            ((SupportMapFragment) fragment).getMapAsync(onMapReadyCallback);
        }
    }

    @BindingAdapter("app:geoMode")
    public static void setGeoMode(TextView view, @Nullable GeoMode geoMode) {
        if (geoMode == null) {
            view.setText(null);
            return;
        }

        switch (geoMode) {
            case POINT:
                view.setText(R.string.geopoint_instruction);
                break;

            case SHAPE:
                view.setText(R.string.geoshape_instruction);
                break;

            case TRACE:
                view.setText(R.string.geotrace_instruction);
                break;
        }
    }
}
