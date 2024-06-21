package org.odk.collect.maps

object AnalyticsEvents {

    /**
     * Tracks how many offline layers people are importing at once
     */
    const val IMPORT_LAYER_SINGLE = "ImportLayerSingle" // One
    const val IMPORT_LAYER_FEW = "ImportLayerFew" // <= 5
    const val IMPORT_LAYER_MANY = "ImportLayerMany" // > 5
}
