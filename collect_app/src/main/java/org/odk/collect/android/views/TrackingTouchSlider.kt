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
    private var listener: OnChangeListener? = null

    private var defaultTickActiveTintList: ColorStateList = tickActiveTintList
    private var defaultThumbWidth = thumbWidth
    private var defaultThumbTrackGapSize = thumbTrackGapSize

    var isTrackingTouch: Boolean = false
        private set

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
                    setTickActiveTintList(defaultTickActiveTintList)
                    setThumbWidth(defaultThumbWidth)
                    setThumbTrackGapSize(defaultThumbTrackGapSize)
                    listener?.onValueChange(this, value, true)
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
