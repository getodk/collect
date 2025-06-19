package org.odk.collect.qrcode

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt

class ScannerOverlay(context: Context, attrs: AttributeSet?) :
    View(context, attrs) {

    private val laserPaint = Paint().also {
        it.color = Color.RED
    }

    private val laserAnim = ValueAnimator.ofFloat(0f, 100f).also { animator ->
        animator.setDuration(1000)
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener {
            laserPaint.alpha = (it.animatedValue as Float).roundToInt()
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val verticalMid = height / 2
        canvas.drawRect(
            0f + 2,
            verticalMid.toFloat() - 2,
            width.toFloat() - 2,
            verticalMid.toFloat() + 2,
            laserPaint
        )

        if (!laserAnim.isStarted) {
            laserAnim.start()
        }
    }
}
