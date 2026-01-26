package org.odk.collect.mapbox

import android.content.Context
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.PolygonDescription
import org.odk.collect.maps.getMarkersForPoints

internal class DynamicPolygonFeature(
    private val context: Context,
    private val pointAnnotationManager: PointAnnotationManager,
    private val polygonAnnotationManager: PolygonAnnotationManager,
    private val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureDragEndListener: MapFragment.FeatureListener?,
    private val polygonDescription: PolygonDescription
) : LineFeature {
    override val points: List<MapPoint>
        get() = _points.toList()

    private val _points = mutableListOf<MapPoint>()
    private val pointAnnotations = mutableListOf<PointAnnotation>()
    private val pointAnnotationClickListener = ClickListener()
    private val pointAnnotationDragListener = DragListener()
    private var polygonAnnotation: PolygonAnnotation? = null

    init {
        val markerDescriptions = polygonDescription.getMarkersForPoints()
        markerDescriptions.forEach {
            _points.add(it.point)
            pointAnnotations.add(
                MapUtils.createPointAnnotation(pointAnnotationManager, context, it)
            )
        }

        updateLine()

        pointAnnotationManager.addClickListener(pointAnnotationClickListener)
        pointAnnotationManager.addDragListener(pointAnnotationDragListener)
        polygonAnnotationManager.addClickListener { annotation ->
            polygonAnnotation?.let {
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
        pointAnnotationManager.apply {
            removeClickListener(pointAnnotationClickListener)
            removeDragListener(pointAnnotationDragListener)
            delete(pointAnnotations)
        }

        polygonAnnotation?.let {
            polygonAnnotationManager.delete(it)
        }

        pointAnnotations.clear()
        _points.clear()
    }

    private fun updateLine() {
        val points = points
            .map {
                Point.fromLngLat(it.longitude, it.latitude, it.altitude)
            }

        polygonAnnotation?.let {
            polygonAnnotationManager.delete(it)
        }

        if (points.size > 1) {
            polygonAnnotation = polygonAnnotationManager.create(
                PolygonAnnotationOptions()
                    .withPoints(listOf(points))
                    .withFillOutlineColor(polygonDescription.getStrokeColor())
                    .withFillColor(polygonDescription.getFillColor())
            ).also {
                polygonAnnotationManager.update(it)
            }
        }
    }

    private inner class ClickListener : OnPointAnnotationClickListener {
        override fun onAnnotationClick(annotation: PointAnnotation): Boolean {
            for (pointAnnotation in pointAnnotations) {
                if (annotation.id == pointAnnotation.id && featureClickListener != null) {
                    featureClickListener.onFeature(featureId)
                    return true
                }
            }
            return false
        }
    }

    private inner class DragListener : OnPointAnnotationDragListener {
        override fun onAnnotationDragStarted(annotation: com.mapbox.maps.plugin.annotation.Annotation<*>) = Unit

        override fun onAnnotationDrag(annotation: com.mapbox.maps.plugin.annotation.Annotation<*>) {
            pointAnnotations.forEachIndexed { index, pointAnnotation ->
                if (annotation.id == pointAnnotation.id) {
                    _points[index] = MapUtils.mapPointFromPointAnnotation(pointAnnotation)
                }
            }
            updateLine()
        }

        override fun onAnnotationDragFinished(annotation: com.mapbox.maps.plugin.annotation.Annotation<*>) {
            onAnnotationDrag(annotation)
            if (featureDragEndListener != null) {
                for (pointAnnotation in pointAnnotations) {
                    if (annotation.id == pointAnnotation.id) {
                        featureDragEndListener.onFeature(featureId)
                        break
                    }
                }
            }
        }
    }
}
