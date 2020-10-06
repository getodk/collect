package org.odk.collect.android.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.slider.Slider;

import org.jetbrains.annotations.NotNull;

public class SuppressFlingGestureSlider extends Slider implements  Slider.OnSliderTouchListener {

    private boolean suppressFlingGesture;

    public SuppressFlingGestureSlider(@NonNull Context context) {
        super(context);
    }

    public SuppressFlingGestureSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private void setSuppressFlingGesture(boolean suppressFlingGesture) {
        this.suppressFlingGesture = suppressFlingGesture;
    }

    public boolean isSuppressFlingGesture() {
        return suppressFlingGesture;
    }

    @Override
    public void onStopTrackingTouch(@NotNull Slider slider) {
        setSuppressFlingGesture(false);
    }

    @Override
    public void onStartTrackingTouch(@NotNull Slider slider) {
        setSuppressFlingGesture(true);
    }
}
