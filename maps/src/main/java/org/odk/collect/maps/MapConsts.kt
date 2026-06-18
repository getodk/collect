package org.odk.collect.maps

import androidx.core.graphics.toColorInt

object MapConsts {
    @JvmField
    val DEFAULT_STROKE_COLOR = "#3e9fcc".toColorInt()

    @JvmField
    val DEFAULT_HIGHLIGHT_COLOR = "#1F5976".toColorInt()

    val DEFAULT_ERROR_COLOR = "#ba1a1a".toColorInt()

    const val DEFAULT_STROKE_WIDTH = 8f
    const val DEFAULT_FILL_COLOR_OPACITY = 68

    /**
     * Returns the stacking order for a feature so that background features are drawn below
     * foreground ones (a higher value is drawn on top). Used as a z-index on Google Maps and as an
     * annotation sort key on Mapbox.
     */
    @JvmStatic
    fun getZIndex(background: Boolean): Int {
        return if (background) 1 else 2
    }
}
