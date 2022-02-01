package org.odk.collect.android.geo

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

object MapsMarkerCache {
    /**
     * We use different markers in different features but it looks like we will never need to
     * support more than 10 different types of markers at the same time.
     */
    private val cache = LruCache<Int, Bitmap>(10)

    @JvmStatic
    fun getMarkerBitmap(@DrawableRes drawable: Int, context: Context): Bitmap {
        if (cache[drawable] == null) {
            ContextCompat.getDrawable(context, drawable)?.toBitmap().also {
                cache.put(drawable, it)
            }
        }
        return cache[drawable]
    }

    @JvmStatic
    fun clearCache() {
        cache.evictAll()
    }
}
