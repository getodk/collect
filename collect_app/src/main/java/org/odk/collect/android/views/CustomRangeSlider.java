package org.odk.collect.android.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.slider.Slider;

import org.jetbrains.annotations.NotNull;

public class CustomRangeSlider extends Slider implements  Slider.OnSliderTouchListener {

    public boolean suppressFlingGesture;

    public CustomRangeSlider(@NonNull Context context) {
        super(context);
    }

    public CustomRangeSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onStopTrackingTouch(@NotNull Slider slider) {
        suppressFlingGesture = false;
    }

    @Override
    public void onStartTrackingTouch(@NotNull Slider slider) {
        suppressFlingGesture = true;
    }
}
