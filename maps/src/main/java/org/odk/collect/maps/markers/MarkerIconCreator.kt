package org.odk.collect.maps.markers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.LruCache
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap

object MarkerIconCreator {
    /**
     * We use different markers in different features and in normal cases we should not need to use
     * more than 10 different types of markers at the same time.
     * The case when markers have symbols is an exception and then it doesn't make sense to cache
     * bitmaps because every marker will most likely be unique.
     */
    private val cache = LruCache<String, Bitmap>(10)

    @JvmStatic
    fun getMarkerIcon(context: Context, markerIconDescription: MarkerIconDescription): Bitmap {
        return when (markerIconDescription) {
            is MarkerIconDescription.LinePoint -> {
                fromCache("LinePoint" + markerIconDescription.lineSize + markerIconDescription.color) {
                    createPoint(
                        markerIconDescription.lineSize * 6,
                        markerIconDescription.lineSize,
                        markerIconDescription.color
                    )
                }
            }

            is MarkerIconDescription.DrawableResource -> {
                val drawableId = markerIconDescription.drawable
                val color = markerIconDescription.getColor()
                val symbol = markerIconDescription.getSymbol()

                val bitmapId = drawableId.toString() + color + symbol
                fromCache(bitmapId) {
                    createBitmap(context, drawableId, color, symbol)
                }
            }
        }
    }

    private fun createPoint(diameter: Float, strokeSize: Float, color: Int): Bitmap {
        val bitmap =
            Bitmap.createBitmap(diameter.toInt(), diameter.toInt(), Config.ARGB_8888)

        Canvas(bitmap).also { canvas ->
            val radius = diameter / 2

            val fill = Paint().also {
                it.style = Paint.Style.FILL
                it.color = Color.parseColor("#ffffff")
            }
            canvas.drawCircle(radius, radius, radius, fill)

            val stroke = Paint().also {
                it.style = Paint.Style.STROKE
                it.color = color
                it.strokeWidth = strokeSize
            }
            canvas.drawCircle(radius, radius, radius - (strokeSize / 2), stroke)
        }

        return bitmap
    }

    private fun fromCache(bitmapId: String, factory: () -> Bitmap): Bitmap {
        return if (cache[bitmapId] == null) {
            factory().also {
                cache.put(bitmapId, it)
            }
        } else {
            cache[bitmapId]
        }
    }

    private fun createBitmap(
        context: Context,
        drawableId: Int,
        color: Int?,
        symbol: String?
    ): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        if (drawable != null) {
            drawable.mutate()

            val isBackgroundDark = color?.let {
                drawable.setTint(it)
                ColorUtils.calculateLuminance(color) < 0.5
            } ?: true

            return drawable.toBitmap().also { bitmap ->
                symbol?.let {
                    val paint = Paint().also {
                        it.style = Paint.Style.FILL
                        it.color = if (isBackgroundDark) Color.WHITE else Color.BLACK
                        it.textSize = (bitmap.width / 2.3).toFloat()
                        it.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        it.textAlign = Paint.Align.CENTER
                    }

                    Canvas(bitmap).also {
                        val x: Int = it.width / 2
                        val y: Int = it.height / 2
                        it.drawText(symbol, x.toFloat(), y.toFloat(), paint)
                    }
                }
            }
        } else {
            throw IllegalStateException("Drawable cannot be created!")
        }
    }

    @JvmStatic
    fun clearCache() {
        cache.evictAll()
    }

    @JvmStatic
    fun MarkerIconDescription.toBitmap(context: Context): Bitmap {
        return getMarkerIcon(context, this)
    }
}
