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
import org.odk.collect.maps.MapConsts
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.PolygonDescription
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription

/** A polygon that can be manipulated by dragging Symbols at its vertices. */
internal class DynamicPolygonFeature(
    private val context: Context,
    private val pointAnnotationManager: PointAnnotationManager,
    private val polygonAnnotationManager: PolygonAnnotationManager,
    private val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureDragEndListener: MapFragment.FeatureListener?,
    private val polygonDescription: PolygonDescription
) : LineFeature {
    override val points = mutableListOf<MapPoint>()
    private val pointAnnotations = mutableListOf<PointAnnotation>()
    private val pointAnnotationClickListener = ClickListener()
    private val pointAnnotationDragListener = DragListener()
    private var polygonAnnotation: PolygonAnnotation? = null

    init {
        polygonDescription.points.forEachIndexed { index, point ->
            points.add(point)

            val markerIconDescription = if (index == polygonDescription.points.lastIndex) {
                MarkerIconDescription.LinePoint(polygonDescription.getStrokeWidth(), MapConsts.DEFAULT_HIGHLIGHT_COLOR)
            } else {
                MarkerIconDescription.LinePoint(polygonDescription.getStrokeWidth(), MapConsts.DEFAULT_STROKE_COLOR)
            }
            pointAnnotations.add(
                MapUtils.createPointAnnotation(
                    pointAnnotationManager,
                    context,
                    MarkerDescription(
                        point,
                        true,
                        MapFragment.CENTER,
                        markerIconDescription
                    )
                )
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
        points.clear()
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
                    points[index] = MapUtils.mapPointFromPointAnnotation(pointAnnotation)
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
