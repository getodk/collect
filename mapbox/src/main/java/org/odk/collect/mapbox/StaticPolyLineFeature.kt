package org.odk.collect.mapbox

import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.OnPolylineAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.traces.LineDescription

/** A polyline that can not be manipulated by dragging Symbols at its vertices. */
internal class StaticPolyLineFeature(
    private val polylineAnnotationManager: PolylineAnnotationManager,
    private val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    lineDescription: LineDescription
) : LineFeature {
    override val points = mutableListOf<MapPoint>()
    private var polylineAnnotation: PolylineAnnotation? = null
    private var clickListener: OnPolylineAnnotationClickListener? = null

    init {
        lineDescription.points.forEach {
            points.add(it)
        }

        val points = points
            .map {
                Point.fromLngLat(it.longitude, it.latitude, it.altitude)
            }
            .toMutableList()

        polylineAnnotation?.let {
            polylineAnnotationManager.delete(it)
        }

        if (points.size > 1) {
            polylineAnnotation = polylineAnnotationManager.create(
                PolylineAnnotationOptions()
                    .withPoints(points)
                    .withLineColor(lineDescription.getStrokeColor())
                    .withLineWidth(MapUtils.convertStrokeWidth(lineDescription))
                    .withLineSortKey(MapUtils.sortKey(lineDescription.background))
            ).also {
                polylineAnnotationManager.update(it)
            }
        }

        if (lineDescription.clickable && featureClickListener != null) {
            clickListener = OnPolylineAnnotationClickListener { annotation ->
                polylineAnnotation?.let {
                    if (annotation.id == it.id) {
                        featureClickListener.onFeature(featureId)
                        true
                    } else {
                        false
                    }
                } ?: false
            }.also(polylineAnnotationManager::addClickListener)
        }
    }

    override fun dispose() {
        polylineAnnotation?.let {
            polylineAnnotationManager.delete(it)
        }
        clickListener?.let(polylineAnnotationManager::removeClickListener)
        points.clear()
    }
}
