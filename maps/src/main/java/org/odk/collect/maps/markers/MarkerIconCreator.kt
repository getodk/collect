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
import org.odk.collect.maps.MapConsts

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
                fromCache("LinePoint") {
                    val size = markerIconDescription.lineSize * 6
                    val bitmap =
                        Bitmap.createBitmap(size.toInt(), size.toInt(), Config.ARGB_8888)

                    Canvas(bitmap).also { canvas ->
                        val radius = size / 2

                        val fill = Paint().also {
                            it.style = Paint.Style.FILL
                            it.color = MapConsts.DEFAULT_STROKE_COLOR
                        }
                        canvas.drawCircle(radius, radius, radius, fill)

                        val strokeWidth = markerIconDescription.lineSize
                        val stroke = Paint().also {
                            it.style = Paint.Style.STROKE
                            it.color = Color.parseColor("#ffffff")
                            it.strokeWidth = strokeWidth
                        }
                        canvas.drawCircle(radius, radius, radius - (strokeWidth / 2), stroke)
                    }

                    bitmap
                }
            }

            is MarkerIconDescription.Resource -> {
                val drawableId = markerIconDescription.icon
                val color = markerIconDescription.getColor()
                val symbol = markerIconDescription.getSymbol()

                val bitmapId = drawableId.toString() + color + symbol
                fromCache(bitmapId) {
                    createBitmap(context, drawableId, color, symbol).also {
                        cache.put(bitmapId, it)
                    }
                }
            }
        }
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
    fun MarkerIconDescription.getBitmap(context: Context): Bitmap {
        return getMarkerIcon(context, this)
    }
}
