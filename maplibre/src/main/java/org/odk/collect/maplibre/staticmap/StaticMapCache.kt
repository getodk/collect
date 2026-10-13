package org.odk.collect.maplibre.staticmap

import android.graphics.Bitmap
import android.util.LruCache
import org.odk.collect.maps.traces.TraceDescription

class StaticMapCache {

    private val cache = LruCache<Key, Bitmap>(10)

    fun get(configuration: StaticMapConfiguration, trace: TraceDescription, width: Int, height: Int): Bitmap? {
        return cache.get(Key(configuration, trace, width, height))
    }

    fun put(configuration: StaticMapConfiguration, trace: TraceDescription, width: Int, height: Int, bitmap: Bitmap) {
        cache.put(Key(configuration, trace, width, height), bitmap)
    }

    private data class Key(
        val configuration: StaticMapConfiguration,
        val trace: TraceDescription,
        val width: Int,
        val height: Int
    )
}
