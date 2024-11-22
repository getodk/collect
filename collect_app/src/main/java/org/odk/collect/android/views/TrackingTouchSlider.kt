package org.odk.collect.android.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.android.material.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.slider.Slider

@SuppressLint("ClickableViewAccessibility")
class TrackingTouchSlider(
    context: Context,
    attrs: AttributeSet?
) : Slider(context, attrs), Slider.OnSliderTouchListener {
    private var defaultTickActiveTintList: ColorStateList = tickActiveTintList
    private var defaultThumbWidth = thumbWidth
    private var defaultThumbTrackGapSize = thumbTrackGapSize

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
        setTickActiveTintList(defaultTickActiveTintList)
        setThumbWidth(defaultThumbWidth)
        setThumbTrackGapSize(defaultThumbTrackGapSize)
        enabled = true
    }

    fun disable() {
        value = valueFrom
        setTickActiveTintList(
            ColorStateList.valueOf(
                MaterialColors.getColor(
                    this,
                    R.attr.colorPrimary
                )
            )
        )
        setThumbWidth(0)
        setThumbTrackGapSize(0)
        enabled = false
    }

    fun setOnFirstValueChanged(onMinValueChangedListener: OnMinValueChangedListener) {
        this.onMinValueChangedListener = onMinValueChangedListener
    }
}
