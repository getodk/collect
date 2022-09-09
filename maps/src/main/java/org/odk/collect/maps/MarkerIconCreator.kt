package org.odk.collect.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.LruCache
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

object MarkerIconCreator {
    /**
     * We use different markers in different features and in normal cases we should not need to use
     * more than 10 different types of markers at the same time.
     * The case when markers have symbols is an exception and then it doesn't make sense to cache
     * bitmaps because every marker will most likely be unique.
     */
    private val cache = LruCache<Int, Bitmap>(10)

    @JvmStatic
    fun getMarkerIconBitmap(context: Context, markerIconDescription: MarkerIconDescription): Bitmap {
        val (drawableId, color, symbol) = markerIconDescription

        val bitmapId = markerIconDescription.hashCode()

        if (cache[bitmapId] == null) {
            ContextCompat.getDrawable(context, drawableId)?.also { drawable ->
                color?.let {
                    try {
                        drawable.setTint(Color.parseColor(it))
                    } catch (e: Throwable) {
                        // ignore
                    }
                }

                drawable.toBitmap().also { bitmap ->
                    symbol?.let {
                        val paint = Paint().also {
                            it.style = Paint.Style.FILL
                            it.color = Color.WHITE
                            it.textSize = 30F
                            it.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            it.textAlign = Paint.Align.CENTER
                        }

                        Canvas(bitmap).also {
                            val x: Int = it.width / 2
                            val y: Int = it.height / 2
                            it.drawText(symbol, x.toFloat(), y.toFloat(), paint)
                        }
                    }

                    cache.put(bitmapId, bitmap)
                }
            }
        }
        return cache[bitmapId]
    }

    @JvmStatic
    fun clearCache() {
        cache.evictAll()
    }
}
