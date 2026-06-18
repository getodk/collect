package org.odk.collect.mapbox

import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.OnPolygonAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPolylineAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import org.odk.collect.maps.MapConsts
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.traces.PolygonDescription

class StaticPolygonFeature(
    private val polygonAnnotationManager: PolygonAnnotationManager,
    private val polylineAnnotationManager: PolylineAnnotationManager,
    polygonDescription: PolygonDescription,
    featureClickListener: MapFragment.FeatureListener?,
    featureId: Int
) : LineFeature {

    override val points: List<MapPoint> = polygonDescription.points

    private val mapboxPoints = points.map { Point.fromLngLat(it.longitude, it.latitude) }

    private val polygonAnnotation = polygonAnnotationManager.create(
        PolygonAnnotationOptions()
            .withPoints(listOf(mapboxPoints))
            .withFillOutlineColor(polygonDescription.getStrokeColor())
            .withFillColor(polygonDescription.getFillColor())
            .withFillSortKey(MapConsts.getZIndex(polygonDescription.background).toDouble())
    )

    private val polylineAnnotation = polylineAnnotationManager.create(
        PolylineAnnotationOptions()
            .withPoints(mapboxPoints)
            .withLineColor(polygonDescription.getStrokeColor())
            .withLineWidth(MapUtils.convertStrokeWidth(polygonDescription))
            .withLineSortKey(MapConsts.getZIndex(polygonDescription.background).toDouble())
    )

    private var polygonClickListener: OnPolygonAnnotationClickListener? = null
    private var polylineClickListener: OnPolylineAnnotationClickListener? = null

    init {
        if (polygonDescription.clickable && featureClickListener != null) {
            polygonClickListener = OnPolygonAnnotationClickListener { annotation ->
                if (annotation.id == polygonAnnotation.id) {
                    featureClickListener.onFeature(featureId)
                    true
                } else {
                    false
                }
            }.also(polygonAnnotationManager::addClickListener)

            polylineClickListener = OnPolylineAnnotationClickListener { annotation ->
                if (annotation.id == polylineAnnotation.id) {
                    featureClickListener.onFeature(featureId)
                    true
                } else {
                    false
                }
            }.also(polylineAnnotationManager::addClickListener)
        }
    }

    override fun dispose() {
        polygonAnnotationManager.run {
            delete(polygonAnnotation)
            polygonClickListener?.let(::removeClickListener)
        }

        polylineAnnotationManager.run {
            delete(polylineAnnotation)
            polylineClickListener?.let(::removeClickListener)
        }
    }
}
