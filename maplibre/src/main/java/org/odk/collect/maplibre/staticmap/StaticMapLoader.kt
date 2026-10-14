package org.odk.collect.maplibre.staticmap

import android.graphics.Bitmap
import org.odk.collect.maps.traces.TraceDescription

interface StaticMapLoader {
    fun load(
        configuration: StaticMapConfiguration,
        cache: StaticMapCache,
        trace: TraceDescription,
        width: Int,
        height: Int,
        callback: (Bitmap?) -> Unit
    ): () -> Unit
}
