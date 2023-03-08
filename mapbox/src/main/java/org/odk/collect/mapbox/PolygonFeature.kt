package org.odk.collect.mapbox

import androidx.core.graphics.ColorUtils
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.OnPolygonAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint

class PolygonFeature(
    private val polygonAnnotationManager: PolygonAnnotationManager,
    points: Iterable<MapPoint>,
    outlineColor: Int,
    featureClickListener: MapFragment.FeatureListener?,
    featureId: Int,
) : MapFeature {

    private var polygon: PolygonAnnotation = polygonAnnotationManager.create(
        PolygonAnnotationOptions()
            .withPoints(listOf(points.map { Point.fromLngLat(it.longitude, it.latitude) }))
            .withFillColor(ColorUtils.setAlphaComponent(outlineColor, 150))
    )

    private val clickListener =
        PolygonClickListener(polygon.id, featureClickListener, featureId).also {
            polygonAnnotationManager.addClickListener(it)
        }

    override fun dispose() {
        polygonAnnotationManager.delete(polygon)
        polygonAnnotationManager.removeClickListener(clickListener)
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
