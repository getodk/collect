package org.odk.collect.geo.analytics

object AnalyticsEvents {

    /**
     * Compare different ways of saving geopoints: are users saving without looking at accuracy,
     * are they waiting for the threshold autosave etc. Each event should include `accuracy` param
     * with either `unacceptable` (over 100m), `poor` (over 10m) or `acceptable` (less than 10m).
     */
    const val SAVE_POINT_AUTO = "SavePointAuto"
    const val SAVE_POINT_MANUAL = "SavePointManual"
    const val SAVE_POINT_IMMEDIATE = "SavePointImmediate"
}
