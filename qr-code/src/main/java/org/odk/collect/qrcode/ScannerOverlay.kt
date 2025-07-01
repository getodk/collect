package org.odk.collect.qrcode

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.roundToInt

class ScannerOverlay(context: Context, attrs: AttributeSet?) :
    View(context, attrs) {

    private val laserPaint = Paint().also {
        it.color = Color.RED
    }

    private val borderPaint = Paint().also {
        it.color = Color.BLACK
        it.alpha = 75
    }

    private val laserAnim = ValueAnimator.ofFloat(0f, 255f).also { animator ->
        animator.setDuration(320)
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
    }

    override fun onDraw(canvas: Canvas) {
        val verticalBorder = max((height - SQUARE_SIZE) / 2f, MIN_BORDER_SIZE)
        val horizontalBorder = max((width - SQUARE_SIZE) / 2f, MIN_BORDER_SIZE)

        drawBorder(canvas, horizontalBorder, verticalBorder)
        drawLaser(canvas, horizontalBorder)
    }

    private fun drawBorder(canvas: Canvas, horizontalSize: Float, verticalSize: Float) {
        // Top
        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            verticalSize,
            borderPaint
        )

        // Left
        canvas.drawRect(
            0f,
            verticalSize,
            horizontalSize,
            height.toFloat() - verticalSize,
            borderPaint
        )

        // Right
        canvas.drawRect(
            width.toFloat() - horizontalSize,
            verticalSize,
            width.toFloat(),
            height.toFloat() - verticalSize,
            borderPaint
        )

        // Bottom
        canvas.drawRect(
            0f,
            height.toFloat() - verticalSize,
            width.toFloat(),
            height.toFloat(),
            borderPaint
        )
    }

    private fun drawLaser(canvas: Canvas, horizontalBorder: Float) {
        val verticalMid = height / 2
        val horizontalMargin = horizontalBorder + 8f

        canvas.drawRect(
            0f + horizontalMargin,
            verticalMid.toFloat() - 2,
            width.toFloat() - horizontalMargin,
            verticalMid.toFloat() + 2,
            laserPaint
        )
    }

    fun startAnimations() {
        laserAnim.addUpdateListener {
            laserPaint.alpha = (it.animatedValue as Float).roundToInt()
            invalidate()
        }

        laserAnim.start()
    }

    fun stopAnimations() {
        laserAnim.cancel()
        laserAnim.removeAllUpdateListeners()
    }

    companion object {
        const val SQUARE_SIZE = 820f
        const val MIN_BORDER_SIZE = 80f
    }
}
