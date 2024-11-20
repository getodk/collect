package org.odk.collect.android.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.slider.Slider;

import org.jetbrains.annotations.NotNull;

public class TrackingTouchSlider extends Slider implements Slider.OnSliderTouchListener {

    private boolean trackingTouch;
    private boolean enabled;

    private OnMinValueChangedListener onMinValueChangedListener;

    public interface OnMinValueChangedListener {
        void onFirstValueChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    public TrackingTouchSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        addOnSliderTouchListener(this);
        setLabelFormatter(null);
        setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    if (!enabled) {
                        onMinValueChangedListener.onFirstValueChanged();
                    }
                    break;
            }
            v.onTouchEvent(event);
            return true;
        });
    }

    public boolean isTrackingTouch() {
        return trackingTouch;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onStopTrackingTouch(@NotNull Slider slider) {
        trackingTouch = false;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onStartTrackingTouch(@NotNull Slider slider) {
        trackingTouch = true;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public void setOnFirstValueChanged(OnMinValueChangedListener onMinValueChangedListener) {
        this.onMinValueChangedListener = onMinValueChangedListener;
    }
}