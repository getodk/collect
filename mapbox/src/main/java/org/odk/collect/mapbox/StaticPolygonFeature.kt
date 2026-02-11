package org.odk.collect.mapbox

import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.OnPolygonAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPolylineAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.PolygonDescription

class StaticPolygonFeature(
    private val polygonAnnotationManager: PolygonAnnotationManager,
    private val polylineAnnotationManager: PolylineAnnotationManager,
    polygonDescription: PolygonDescription,
    featureClickListener: MapFragment.FeatureListener?,
    featureId: Int
) : LineFeature {

    override val points: List<MapPoint> = polygonDescription.points

    private val polygonAnnotation: PolygonAnnotation = polygonAnnotationManager.create(
        PolygonAnnotationOptions()
            .withPoints(listOf(polygonDescription.points.map { Point.fromLngLat(it.longitude, it.latitude) }))
            .withFillOutlineColor(polygonDescription.getStrokeColor())
            .withFillColor(polygonDescription.getFillColor())
    )

    private val polygonClickListener =
        PolygonClickListener(polygonAnnotation.id, featureClickListener, featureId).also {
            polygonAnnotationManager.addClickListener(it)
        }

    private val polylineAnnotation = polylineAnnotationManager.create(
        PolylineAnnotationOptions()
            .withPoints(polygonDescription.points.map { Point.fromLngLat(it.longitude, it.latitude) })
            .withLineColor(polygonDescription.getStrokeColor())
            .withLineWidth(MapUtils.convertStrokeWidth(polygonDescription))
    )

    private val polylineClickListener =
        PolylineClickListener(polylineAnnotation.id, featureClickListener, featureId).also {
            polylineAnnotationManager.addClickListener(it)
        }

    override fun dispose() {
        polygonAnnotationManager.delete(polygonAnnotation)
        polygonAnnotationManager.removeClickListener(polygonClickListener)
        polylineAnnotationManager.delete(polylineAnnotation)
        polylineAnnotationManager.removeClickListener(polylineClickListener)
    }
}

private class PolygonClickListener(
    private val polygonId: Long,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureId: Int
) : OnPolygonAnnotationClickListener {
    override fun onAnnotationClick(annotation: PolygonAnnotation): Boolean {
        return if (annotation.id == polygonId && featureClickListener != null) {
            featureClickListener.onFeature(featureId)
            true
        } else {
            false
        }
    }
}

private class PolylineClickListener(
    private val polygonId: Long,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureId: Int
) : OnPolylineAnnotationClickListener {
    override fun onAnnotationClick(annotation: PolylineAnnotation): Boolean {
        return if (annotation.id == polygonId && featureClickListener != null) {
            featureClickListener.onFeature(featureId)
            true
        } else {
            false
        }
    }
}
