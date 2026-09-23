package org.odk.collect.maps

import android.graphics.Bitmap
import org.odk.collect.maps.traces.TraceDescription

/**
 * Renders a static image of a map showing a line or a polygon. Lets an answer be previewed
 * without an interactive map.
 */
interface MapPreviewRenderer {
    fun render(
        trace: TraceDescription,
        width: Int,
        height: Int,
        callback: (Bitmap?) -> Unit
    ): () -> Unit
}
