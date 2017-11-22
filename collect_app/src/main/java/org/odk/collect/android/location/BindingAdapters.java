package org.odk.collect.android.location;

import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.TextView;

import javax.annotation.Nullable;

import io.reactivex.Observer;

public class BindingAdapters {

    @BindingAdapter("app:geoMode")
    public static void setGeoMode(TextView view, @Nullable GeoMode geoMode) {
    }

    @BindingAdapter("app:clickObserver")
    public static void setClickObserver(View view, Observer<Object> observer) {
        view.setOnClickListener(observer::onNext);
    }

    @BindingAdapter("app:isPresent")
    public static void setIsPresent(View view, boolean isPresent) {
        view.setVisibility(isPresent ? View.VISIBLE: View.GONE);
    }
}
