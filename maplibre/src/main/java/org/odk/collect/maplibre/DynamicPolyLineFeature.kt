package org.odk.collect.maplibre

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.plugins.annotation.Line
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import org.maplibre.android.plugins.annotation.OnSymbolClickListener
import org.maplibre.android.plugins.annotation.OnSymbolDragListener
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.utils.ColorUtils
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.traces.LineDescription
import org.odk.collect.maps.traces.getMarkersForPoints

internal class DynamicPolyLineFeature(
    private val context: Context,
    private val styleIcons: StyleIcons,
    private val symbolManager: SymbolManager,
    private val lineManager: LineManager,
    private val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureDragEndListener: MapFragment.FeatureListener?,
    private val lineDescription: LineDescription
) : LineFeature {
    private val mainHandler = Handler(Looper.getMainLooper())

    override val points: List<MapPoint>
        get() = _points.toList()

    private val _points = mutableListOf<MapPoint>()
    private val symbols = mutableListOf<Symbol>()
    private val symbolClickListener = ClickListener()
    private val symbolDragListener = DragListener()
    private var line: Line? = null

    init {
        val markerDescriptions = lineDescription.getMarkersForPoints()
        markerDescriptions.forEach {
            _points.add(it.point)
            symbols.add(
                MapUtils.createSymbol(symbolManager, styleIcons, context, it)
            )
        }

        updateLine()

        symbolManager.addClickListener(symbolClickListener)
        symbolManager.addDragListener(symbolDragListener)
        lineManager.addClickListener { annotation ->
            line?.let {
                if (annotation.id == it.id && featureClickListener != null) {
                    featureClickListener.onFeature(featureId)
                    true
                } else {
                    false
                }
            } ?: false
        }
    }

    override fun dispose() {
        symbolManager.apply {
            removeClickListener(symbolClickListener)
            removeDragListener(symbolDragListener)
            delete(symbols)
        }

        line?.let {
            lineManager.delete(it)
        }

        symbols.clear()
        _points.clear()
    }

    private fun updateLine() {
        val points = points
            .map {
                LatLng(it.latitude, it.longitude, it.altitude)
            }
            .toMutableList()

        line?.let {
            lineManager.delete(it)
        }

        if (points.size > 1) {
            line = lineManager.create(
                LineOptions()
                    .withLatLngs(points)
                    .withLineColor(ColorUtils.colorToRgbaString(lineDescription.getStrokeColor()))
                    .withLineWidth(MapUtils.convertStrokeWidth(lineDescription))
            ).also {
                lineManager.update(it)
            }
        }
    }

    private inner class ClickListener : OnSymbolClickListener {
        override fun onAnnotationClick(annotation: Symbol): Boolean {
            for (symbol in symbols) {
                if (annotation.id == symbol.id && featureClickListener != null) {
                    featureClickListener.onFeature(featureId)
                    return true
                }
            }
            return false
        }
    }

    private inner class DragListener : OnSymbolDragListener {
        override fun onAnnotationDragStarted(annotation: Symbol) = Unit

        override fun onAnnotationDrag(annotation: Symbol) {
            symbols.forEachIndexed { index, symbol ->
                if (annotation.id == symbol.id) {
                    _points[index] = MapUtils.mapPointFromSymbol(symbol)
                }
            }
            updateLine()
        }

        override fun onAnnotationDragFinished(annotation: Symbol) {
            onAnnotationDrag(annotation)
            if (featureDragEndListener != null) {
                for (symbol in symbols) {
                    if (annotation.id == symbol.id) {
                        // Deferred to avoid ConcurrentModificationException caused by MapLibre iterating over
                        // its annotation list while this callback disposes and recreates annotations.
                        mainHandler.post {
                            featureDragEndListener.onFeature(featureId)
                        }
                        break
                    }
                }
            }
        }
    }
}
