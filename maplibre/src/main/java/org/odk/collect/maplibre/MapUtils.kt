package org.odk.collect.maplibre

import android.content.Context
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.Property
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.traces.TraceDescription

object MapUtils {
    fun createSymbol(
        symbolManager: SymbolManager,
        styleIcons: StyleIcons,
        context: Context,
        markerDescription: MarkerDescription
    ): Symbol {
        return symbolManager.create(symbolOptions(styleIcons, context, markerDescription))
    }

    fun createSymbols(
        context: Context,
        symbolManager: SymbolManager,
        styleIcons: StyleIcons,
        markerFeatures: List<MarkerDescription>
    ): List<Symbol> {
        val symbolOptionsList = markerFeatures.map {
            symbolOptions(styleIcons, context, it)
        }

        return symbolManager.create(symbolOptionsList)
    }

    private fun symbolOptions(
        styleIcons: StyleIcons,
        context: Context,
        markerDescription: MarkerDescription
    ): SymbolOptions {
        return SymbolOptions()
            .withLatLng(
                LatLng(
                    markerDescription.point.latitude,
                    markerDescription.point.longitude,
                    markerDescription.point.altitude
                )
            )
            .withIconImage(styleIcons.add(context, markerDescription.iconDescription))
            .withIconSize(1.0f)
            .withSymbolSortKey(sortKey(markerDescription.iconDescription.background))
            .withDraggable(markerDescription.isDraggable)
            .withTextOpacity(0.0f)
            .withIconAnchor(getIconAnchorValue(markerDescription.iconAnchor))
    }

    private fun sortKey(background: Boolean): Float {
        return if (background) 1.0f else 2.0f
    }

    private fun getIconAnchorValue(iconAnchor: MapFragment.IconAnchor): String {
        return when (iconAnchor) {
            MapFragment.IconAnchor.BOTTOM -> Property.ICON_ANCHOR_BOTTOM
            else -> Property.ICON_ANCHOR_CENTER
        }
    }

    fun mapPointFromSymbol(symbol: Symbol): MapPoint {
        // When a symbol is manually dragged, the position is no longer
        // obtained from a GPS reading, so the altitude and standard
        // deviation fields are no longer meaningful; reset them to zero.
        return MapPoint(symbol.latLng.latitude, symbol.latLng.longitude, 0.0, 0.0)
    }

    // To ensure consistent stroke width across map platforms like Google Maps, the value for
    // MapLibre needs to be divided by 3.
    fun convertStrokeWidth(traceDescription: TraceDescription): Float {
        return (traceDescription.getStrokeWidth() / 3)
    }
}
