package org.odk.collect.maplibre

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.plugins.annotation.Line
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import org.maplibre.android.plugins.annotation.OnLineClickListener
import org.maplibre.android.utils.ColorUtils
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.traces.LineDescription

/** A polyline that can not be manipulated by dragging Symbols at its vertices. */
internal class StaticPolyLineFeature(
    private val lineManager: LineManager,
    private val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    lineDescription: LineDescription
) : LineFeature {
    override val points = mutableListOf<MapPoint>()
    private var line: Line? = null
    private var clickListener: OnLineClickListener? = null

    init {
        lineDescription.points.forEach {
            points.add(it)
        }

        val points = points
            .map {
                LatLng(it.latitude, it.longitude, it.altitude)
            }
            .toMutableList()

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

        if (lineDescription.clickable && featureClickListener != null) {
            clickListener = OnLineClickListener { annotation ->
                line?.let {
                    if (annotation.id == it.id) {
                        featureClickListener.onFeature(featureId)
                        true
                    } else {
                        false
                    }
                } ?: false
            }.also(lineManager::addClickListener)
        }
    }

    override fun dispose() {
        line?.let {
            lineManager.delete(it)
        }
        clickListener?.let(lineManager::removeClickListener)
        points.clear()
    }
}
