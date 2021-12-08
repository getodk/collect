package org.odk.collect.geo.analytics

object AnalyticsEvents {

    /**
     * Compare different ways of saving geopoints: are users saving without looking at accuracy,
     * are they waiting for the threshold autosave etc
     */
    const val SAVE_POINT_AUTO = "SavePointAuto"
    const val SAVE_POINT_MANUAL_IMMEDIATE = "SavePointManualImmediate"
    const val SAVE_POINT_MANUAL_UNACCEPTABLE = "SavePointManualUnacceptable"
    const val SAVE_POINT_MANUAL_POOR = "SavePointManualPoor"
    const val SAVE_POINT_MANUAL_ACCEPTABLE = "SavePointManualAcceptable"
}
