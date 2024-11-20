package org.odk.collect.androidtest

import android.view.MotionEvent
import com.google.android.material.slider.Slider

fun Slider.clickOnStep(step: Float) {
    val xPosition: Float = step * (width / valueTo)
    val yPosition: Float = height / 2f

    val currentTime = System.currentTimeMillis()

    val downEvent = MotionEvent.obtain(
        currentTime,
        currentTime,
        MotionEvent.ACTION_DOWN,
        xPosition,
        yPosition,
        0
    )
    dispatchTouchEvent(downEvent)

    val upEvent = MotionEvent.obtain(
        currentTime,
        currentTime,
        MotionEvent.ACTION_UP,
        xPosition,
        yPosition,
        0
    )
    dispatchTouchEvent(upEvent)

    downEvent.recycle()
    upEvent.recycle()
}
