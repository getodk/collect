package org.odk.collect.testshared

import android.view.MotionEvent
import com.google.android.material.slider.Slider

fun Slider.clickOnMinValue() {
    clickOnPosition(0f)
}

fun Slider.clickOnMaxValue() {
    clickOnPosition(width.toFloat())
}

fun Slider.clickOnPosition(xPosition: Float) {
    val currentTime = System.currentTimeMillis()

    val downEvent = MotionEvent.obtain(
        currentTime,
        currentTime,
        MotionEvent.ACTION_DOWN,
        xPosition,
        0f,
        0
    )
    dispatchTouchEvent(downEvent)

    val upEvent = MotionEvent.obtain(
        currentTime,
        currentTime,
        MotionEvent.ACTION_UP,
        xPosition,
        0f,
        0
    )
    dispatchTouchEvent(upEvent)

    downEvent.recycle()
    upEvent.recycle()
}
