package org.odk.collect.maplibre

import android.content.Context
import android.graphics.Bitmap
import org.maplibre.android.maps.Style
import org.odk.collect.maps.markers.MarkerIconCreator.toBitmap
import org.odk.collect.maps.markers.MarkerIconDescription

/**
 * Symbols refer to their icon by name and the icons themselves belong to the style, so they all
 * have to be added again whenever a new style is loaded. Keeping them here means existing symbols
 * do not lose their icons when the basemap or the reference layer changes.
 */
class StyleIcons(private val currentStyle: () -> Style?) {

    private val bitmaps = mutableMapOf<String, Bitmap>()

    fun onStyleLoaded(style: Style) {
        bitmaps.forEach { (name, bitmap) ->
            style.addImage(name, bitmap)
        }
    }

    /** Returns the name that a symbol can use to refer to this icon. */
    fun add(context: Context, iconDescription: MarkerIconDescription): String {
        val name = iconDescription.toString()

        if (name !in bitmaps) {
            val bitmap = iconDescription.toBitmap(context)
            bitmaps[name] = bitmap
            currentStyle()?.addImage(name, bitmap)
        }

        return name
    }
}
