package org.odk.collect.mapbox

import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.OnPolygonAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.PolygonDescription

class StaticPolygonFeature(
    private val polygonAnnotationManager: PolygonAnnotationManager,
    polygonDescription: PolygonDescription,
    featureClickListener: MapFragment.FeatureListener?,
    featureId: Int
) : MapFeature {

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

    override fun dispose() {
        polygonAnnotationManager.delete(polygonAnnotation)
        polygonAnnotationManager.removeClickListener(polygonClickListener)
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
