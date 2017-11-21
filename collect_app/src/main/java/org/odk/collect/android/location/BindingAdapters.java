package org.odk.collect.android.location;

import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.w3c.dom.Text;

import javax.annotation.Nullable;

public class BindingAdapters {
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


    @BindingAdapter("app:isPresent")
    public static void setIsPresent(View view, boolean isPresent) {
        view.setVisibility(isPresent ? View.VISIBLE: View.GONE);
    }
}
