package org.odk.collect.androidshared.system

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat

object ContextUtils {

    @JvmStatic
    fun getThemeAttributeValue(context: Context, @AttrRes resId: Int): Int {
        val outValue = TypedValue()
        context.theme.resolveAttribute(resId, outValue, true)
        return outValue.data
    }

    /** Renders a Drawable (such as a vector drawable) into a Bitmap.  */
    @JvmStatic
    fun getBitmap(context: Context, drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        if (drawable != null) {
            // shortcut if it's already a bitmap
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                if (bitmap != null) {
                    return bitmap
                }
            }

            var width = drawable.intrinsicWidth
            var height = drawable.intrinsicHeight

            // negative if Drawable is a solid colour
            if (width <= 0 || height <= 0) {
                height = 1
                width = height
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } else {
            return null
        }
    }
}
