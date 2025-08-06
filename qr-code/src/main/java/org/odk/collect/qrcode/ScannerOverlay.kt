package org.odk.collect.qrcode

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.roundToInt

internal class ScannerOverlay(context: Context, attrs: AttributeSet?) :
    View(context, attrs) {

    private val laserPaint = Paint().also {
        it.color = Color.RED
    }

    private val borderPaint = Paint().also {
        it.color = Color.BLACK
        it.alpha = 75
    }

    private val viewFinderPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    var viewFinderRect = Rect()
        private set

    init {
        // Provides better performance for using `PorterDuff.Mode.CLEAR`
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    private val laserAnim = ValueAnimator.ofFloat(0f, 255f).also { animator ->
        animator.duration = 320
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
    }

    override fun onDraw(canvas: Canvas) {
        drawBorder(canvas)
        drawLaser(canvas)
    }

    private fun drawBorder(canvas: Canvas) {
        val verticalBorder = max((height - VIEW_FINDER_SIZE) / 2f, MIN_BORDER_SIZE).toInt()
        val horizontalBorder = max((width - VIEW_FINDER_SIZE) / 2f, MIN_BORDER_SIZE).toInt()
        viewFinderRect.set(
            horizontalBorder,
            verticalBorder,
            this.width - horizontalBorder,
            this.height - verticalBorder
        )

        // Draw full screen semi-transparent overlay
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), borderPaint)

        // Clear the center
        canvas.drawRect(
            viewFinderRect.left.toFloat(),
            viewFinderRect.top.toFloat(),
            viewFinderRect.right.toFloat(),
            viewFinderRect.bottom.toFloat(),
            viewFinderPaint
        )
    }

    private fun drawLaser(canvas: Canvas) {
        val verticalMid = height / 2
        val horizontalMargin = viewFinderRect.left + 8f

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
        const val VIEW_FINDER_SIZE = 820f
        const val MIN_BORDER_SIZE = 80f
    }
}
