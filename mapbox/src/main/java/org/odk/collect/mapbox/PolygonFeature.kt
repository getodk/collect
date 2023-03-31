package org.odk.collect.mapbox

import android.content.Context
import androidx.core.graphics.ColorUtils
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPolygonAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint

class PolygonFeature(
    private val context: Context,
    private val pointAnnotationManager: PointAnnotationManager,
    private val polygonAnnotationManager: PolygonAnnotationManager,
    points: Iterable<MapPoint>,
    featureClickListener: MapFragment.FeatureListener?,
    featureId: Int
) : MapFeature {

    private val polygonAnnotation: PolygonAnnotation = polygonAnnotationManager.create(
        PolygonAnnotationOptions()
            .withPoints(listOf(points.map { Point.fromLngLat(it.longitude, it.latitude) }))
            .withFillColor(
                ColorUtils.setAlphaComponent(
                    context.resources.getColor(R.color.mapLineColor),
                    68
                )
            )
    )

    private val polygonClickListener =
        PolygonClickListener(polygonAnnotation.id, featureClickListener, featureId).also {
            polygonAnnotationManager.addClickListener(it)
        }

    private val pointAnnotations = points.map {
        MapUtils.createPointAnnotation(
            pointAnnotationManager,
            it,
            false,
            MapFragment.CENTER,
            R.drawable.ic_map_point,
            context
        )
    }

    private val pointClickListener = PointClickListener(
        pointAnnotations,
        featureClickListener,
        featureId
    ).also { pointAnnotationManager.addClickListener(it) }

    override fun dispose() {
        polygonAnnotationManager.delete(polygonAnnotation)
        polygonAnnotationManager.removeClickListener(polygonClickListener)
        pointAnnotationManager.removeClickListener(pointClickListener)
    }
}

private class PointClickListener(
    private val pointAnnotations: List<PointAnnotation>,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureId: Int
) : OnPointAnnotationClickListener {
    override fun onAnnotationClick(annotation: PointAnnotation): Boolean {
        return if (pointAnnotations.any { it.id == annotation.id } && featureClickListener != null) {
            featureClickListener.onFeature(featureId)
            true
        } else {
            false
        }
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
