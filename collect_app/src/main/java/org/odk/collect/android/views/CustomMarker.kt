package org.odk.collect.android.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import kotlin.math.max

abstract class MeasurableInAdvanceView(context: Context) : View(context) {
    abstract fun measureDesiredSize(): Pair<Int, Int>
}

class CustomMarker(
    context: Context,
    private val name: String,
    private val address: String,
    private val drawable: Drawable,
) : MeasurableInAdvanceView(context) {

    companion object {
        private const val TEXT_BOX_PADDING = 24
        private const val TEXT_BOX_CORNERS = 4f

        private const val IMAGE_TEXT_INSET = 12
        private const val TEXT_SIZE = 28f
    }

    private val bgPaint = Paint()
    private val textPaint = Paint()

    init {
        bgPaint.color = Color.WHITE

        textPaint.apply {
            color = Color.BLUE
            textSize = TEXT_SIZE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
    }

    override fun measureDesiredSize(): Pair<Int, Int> {
        val desiredWidth: Int = max(
            2 * TEXT_BOX_PADDING + max(
                textPaint.measureText(name),
                textPaint.measureText(address)
            ).toInt(),
            drawable.intrinsicWidth
        )

        val desiredHeight: Int = 2 * TEXT_BOX_PADDING +
                2 * textPaint.textSize.toInt() +
                IMAGE_TEXT_INSET +
                drawable.intrinsicHeight

        return desiredWidth to desiredHeight
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        fun drawImage(imageTop: Float) {
            val bitmap = getBitmapFromVectorDrawable(
                drawable,
                drawable.intrinsicWidth,
                drawable.intrinsicHeight
            )
            canvas.drawBitmap(bitmap, (width - drawable.intrinsicWidth) / 2f, imageTop, null)
        }

        when {
            name.isBlank() && address.isBlank() -> {
                drawImage(0f)
            }
            name.isBlank() -> {
                val backgroundY = 2 * TEXT_BOX_PADDING + textPaint.textSize
                val rect = RectF(0f, 0f, width.toFloat(), backgroundY)
                canvas.drawRoundRect(rect, TEXT_BOX_CORNERS, TEXT_BOX_CORNERS, bgPaint)

                val addressY = TEXT_BOX_PADDING + textPaint.textSize
                canvas.drawText(address, width / 2f, addressY, textPaint)

                drawImage(
                    2 * TEXT_BOX_PADDING + textPaint.textSize + IMAGE_TEXT_INSET
                )
            }
            address.isBlank() -> {
                val backgroundY = 2 * TEXT_BOX_PADDING + textPaint.textSize
                val rect = RectF(0f, 0f, width.toFloat(), backgroundY)
                canvas.drawRoundRect(rect, TEXT_BOX_CORNERS, TEXT_BOX_CORNERS, bgPaint)

                val addressY = TEXT_BOX_PADDING + textPaint.textSize
                canvas.drawText(name, width / 2f, addressY, textPaint)

                drawImage(
                    2 * TEXT_BOX_PADDING + textPaint.textSize + IMAGE_TEXT_INSET
                )
            }
            else -> {
                val backgroundY = 2 * (TEXT_BOX_PADDING + textPaint.textSize)
                val rect = RectF(0f, 0f, width.toFloat(), backgroundY)
                canvas.drawRoundRect(rect, TEXT_BOX_CORNERS, TEXT_BOX_CORNERS, bgPaint)

                val nameY = TEXT_BOX_PADDING.toFloat() + textPaint.textSize
                canvas.drawText(name, width / 2f, nameY, textPaint)

                val addressY = nameY + textPaint.textSize
                canvas.drawText(address, width / 2f, addressY, textPaint)

                drawImage(
                    2 * (TEXT_BOX_PADDING + textPaint.textSize) + IMAGE_TEXT_INSET
                )
            }
        }
    }

    private fun getBitmapFromVectorDrawable(drawable: Drawable, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }
}