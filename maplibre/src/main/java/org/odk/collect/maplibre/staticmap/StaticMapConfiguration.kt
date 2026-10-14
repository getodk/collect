package org.odk.collect.maplibre.staticmap

import java.io.File

/**
 * The basemap and optional reference layer a static map is rendered with.
 */
data class StaticMapConfiguration(
    val basemapSource: String,
    val basemapStyle: String?,
    val referenceLayer: File?
)
