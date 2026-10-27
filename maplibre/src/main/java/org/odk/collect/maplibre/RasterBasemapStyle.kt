package org.odk.collect.maplibre

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet

fun rasterBasemapStyle(tileUri: String, attribution: String?, maxZoom: Float?): Style.Builder {
    val tileSet = TileSet("2.1.0", tileUri).apply {
        this.attribution = attribution ?: ""
        scheme = "xyz"
        // Above the server's highest zoom level MapLibre scales up its tiles instead of requesting missing ones
        maxZoom?.let { this.maxZoom = it }
    }

    return Style.Builder()
        // Raster basemaps serve 256px tiles, while MapLibre assumes 512px ones by default
        .withSource(RasterSource("basemap_source", tileSet, 256))
        .withLayer(RasterLayer("basemap_layer", "basemap_source"))
}
