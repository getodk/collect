package org.odk.collect.maplibre

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.plugins.annotation.FillManager
import org.maplibre.android.plugins.annotation.FillOptions
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import org.maplibre.android.plugins.annotation.OnFillClickListener
import org.maplibre.android.plugins.annotation.OnLineClickListener
import org.maplibre.android.utils.ColorUtils
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.traces.PolygonDescription

class StaticPolygonFeature(
    private val fillManager: FillManager,
    private val lineManager: LineManager,
    polygonDescription: PolygonDescription,
    featureClickListener: MapFragment.FeatureListener?,
    featureId: Int
) : LineFeature {

    override val points: List<MapPoint> = polygonDescription.points

    private val latLngs = points.map { LatLng(it.latitude, it.longitude) }
    private val outlineLatLngs = if (latLngs.size > 1 && latLngs.first() != latLngs.last()) {
        latLngs + latLngs.first()
    } else {
        latLngs
    }

    private val fill = fillManager.create(
        FillOptions()
            .withLatLngs(listOf(latLngs))
            .withFillOutlineColor(ColorUtils.colorToRgbaString(polygonDescription.getStrokeColor()))
            .withFillColor(ColorUtils.colorToRgbaString(polygonDescription.getFillColor()))
    )

    private val line = lineManager.create(
        LineOptions()
            .withLatLngs(outlineLatLngs)
            .withLineColor(ColorUtils.colorToRgbaString(polygonDescription.getStrokeColor()))
            .withLineWidth(MapUtils.convertStrokeWidth(polygonDescription))
    )

    private var fillClickListener: OnFillClickListener? = null
    private var lineClickListener: OnLineClickListener? = null

    init {
        if (polygonDescription.clickable && featureClickListener != null) {
            fillClickListener = OnFillClickListener { annotation ->
                if (annotation.id == fill.id) {
                    featureClickListener.onFeature(featureId)
                    true
                } else {
                    false
                }
            }.also(fillManager::addClickListener)

            lineClickListener = OnLineClickListener { annotation ->
                if (annotation.id == line.id) {
                    featureClickListener.onFeature(featureId)
                    true
                } else {
                    false
                }
            }.also(lineManager::addClickListener)
        }
    }

    override fun dispose() {
        fillManager.run {
            delete(fill)
            fillClickListener?.let(::removeClickListener)
        }

        lineManager.run {
            delete(line)
            lineClickListener?.let(::removeClickListener)
        }
    }
}
