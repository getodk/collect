package org.odk.collect.android.geo

import android.content.Context
import android.util.LruCache
import androidx.annotation.DrawableRes
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import org.odk.collect.maps.MapsMarkerCache

object BitmapDescriptorCache {
    /**
     * We use different markers in different features but it looks like we will never need to
     * support more than 10 different types of markers at the same time.
     */
    private val cache = LruCache<Int, BitmapDescriptor>(10)

    @JvmStatic
    fun getBitmapDescriptor(@DrawableRes drawable: Int, context: Context): BitmapDescriptor {
        if (cache[drawable] == null) {
            BitmapDescriptorFactory.fromBitmap(MapsMarkerCache.getMarkerBitmap(drawable, context)).also {
                cache.put(drawable, it)
            }
        }
        return cache[drawable]
    }

    @JvmStatic
    fun clearCache() {
        MapsMarkerCache.clearCache()
        cache.evictAll()
    }
}
