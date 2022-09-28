package org.odk.collect.maps.markers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
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
    fun getMarkerIconDrawable(context: Context, markerIconDescription: MarkerIconDescription) =
        BitmapDrawable(context.resources, getMarkerIconBitmap(context, markerIconDescription))

    @JvmStatic
    fun getMarkerIconBitmap(context: Context, markerIconDescription: MarkerIconDescription): Bitmap {
        val drawableId = markerIconDescription.icon
        val color = markerIconDescription.getColor()
        val symbol = markerIconDescription.getSymbol()

        val bitmapId = drawableId.toString() + color + symbol

        return if (cache[bitmapId] == null) {
            createBitmap(context, drawableId, color, symbol).also {
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
        symbol: String?,
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
}
