package org.odk.collect.android.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.android.material.slider.Slider

@SuppressLint("ClickableViewAccessibility")
class TrackingTouchSlider(
    context: Context,
    attrs: AttributeSet?
) : Slider(context, attrs), Slider.OnSliderTouchListener {
    var isTrackingTouch: Boolean = false
        private set

    private var enabled = false

    private lateinit var onMinValueChangedListener: OnMinValueChangedListener

    interface OnMinValueChangedListener {
        fun onFirstValueChanged()
    }

    init {
        addOnSliderTouchListener(this)
        setLabelFormatter(null)
        setOnTouchListener { v: View, event: MotionEvent ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> v.parent.requestDisallowInterceptTouchEvent(
                    true
                )

                MotionEvent.ACTION_UP -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    if (!enabled) {
                        onMinValueChangedListener.onFirstValueChanged()
                    }
                }
            }
            v.onTouchEvent(event)
            true
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onStopTrackingTouch(slider: Slider) {
        isTrackingTouch = false
    }

    @SuppressLint("RestrictedApi")
    override fun onStartTrackingTouch(slider: Slider) {
        isTrackingTouch = true
    }

    fun enable() {
        enabled = true
    }

    fun disable() {
        enabled = false
    }

    fun setOnFirstValueChanged(onMinValueChangedListener: OnMinValueChangedListener) {
        this.onMinValueChangedListener = onMinValueChangedListener
    }
}
