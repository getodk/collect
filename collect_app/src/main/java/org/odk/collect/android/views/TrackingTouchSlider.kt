package org.odk.collect.android.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.slider.Slider

@SuppressLint("ClickableViewAccessibility")
class TrackingTouchSlider(
    context: Context,
    attrs: AttributeSet?
) : Slider(context, attrs), Slider.OnSliderTouchListener {
    private var listener: OnChangeListener? = null

    private var defaultTickActiveTintList: ColorStateList = tickActiveTintList
    private var defaultThumbWidth = thumbWidth
    private var defaultThumbTrackGapSize = thumbTrackGapSize

    var isTrackingTouch: Boolean = false
        private set

    init {
        addOnSliderTouchListener(this)
        setLabelFormatter(null)
        addOnSliderTouchListener(object : OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                setTickActiveTintList(defaultTickActiveTintList)
                setThumbWidth(defaultThumbWidth)
                setThumbTrackGapSize(defaultThumbTrackGapSize)
                listener?.onValueChange(this@TrackingTouchSlider, value, true)
            }
        })
    }

    @SuppressLint("RestrictedApi")
    override fun onStopTrackingTouch(slider: Slider) {
        isTrackingTouch = false
    }

    @SuppressLint("RestrictedApi")
    override fun onStartTrackingTouch(slider: Slider) {
        isTrackingTouch = true
    }

    fun reset() {
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
    }

    fun setListener(listener: OnChangeListener) {
        this.listener = listener
    }
}
