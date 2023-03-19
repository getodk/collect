package org.odk.collect.android.geo

import android.content.Context
import android.util.LruCache
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import org.odk.collect.maps.markers.MarkerIconCreator
import org.odk.collect.maps.markers.MarkerIconDescription

object BitmapDescriptorCache {
    /**
     * We use different markers in different features and in normal cases we should not need to use
     * more than 10 different types of markers at the same time.
     * The case when markers have symbols is an exception and then it doesn't make sense to cache
     * bitmaps because every marker will most likely be unique.
     */
    private val cache = LruCache<Int, BitmapDescriptor>(10)

    @JvmStatic
    fun getBitmapDescriptor(context: Context, markerIconDescription: MarkerIconDescription): BitmapDescriptor {
        val drawableId = markerIconDescription.hashCode()

        if (cache[drawableId] == null) {
            BitmapDescriptorFactory.fromBitmap(MarkerIconCreator.getMarkerIconBitmap(context, markerIconDescription)).also {
                cache.put(drawableId, it)
            }
        }
        return cache[drawableId]
    }

    @JvmStatic
    fun clearCache() {
        MarkerIconCreator.clearCache()
        cache.evictAll()
    }
}
