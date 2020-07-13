package org.odk.collect.android.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.slider.Slider;

import org.jetbrains.annotations.NotNull;

public class TrackingTouchSlider extends Slider implements  Slider.OnSliderTouchListener {

    private boolean trackingTouch;

    public TrackingTouchSlider(@NonNull Context context) {
        super(context);
    }

    public TrackingTouchSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isTrackingTouch() {
        return trackingTouch;
    }

    @Override
    public void onStopTrackingTouch(@NotNull Slider slider) {
        trackingTouch = false;
    }

    @Override
    public void onStartTrackingTouch(@NotNull Slider slider) {
        trackingTouch = true;
    }
}
