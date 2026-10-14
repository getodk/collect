package org.odk.collect.maplibre

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import org.odk.collect.shared.settings.Settings

fun Configuration.basemapStyle(settings: Settings): String? {
    return styleSetting?.let { settings.getString(it) }
}

fun Configuration.basemapUri(style: String?): BasemapUri {
    return if (uri != null) {
        uri
    } else if (styleSetting != null) {
        styleOptions.getValue(style!!).uri
    } else {
        throw IllegalArgumentException("Invalid Configuration!")
    }
}

fun Configuration.rasterBasemapStyle(tileUri: String): Style.Builder {
    val tileSet = TileSet("2.1.0", tileUri).apply {
        attribution = this@rasterBasemapStyle.attribution ?: ""
        scheme = "xyz"
    }

    return Style.Builder()
        // Raster basemaps serve 256px tiles, while MapLibre assumes 512px ones by default
        .withSource(RasterSource("basemap_source", tileSet, 256))
        .withLayer(RasterLayer("basemap_layer", "basemap_source"))
}
