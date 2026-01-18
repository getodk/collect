package org.odk.collect.mapbox

import android.content.Context
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import org.odk.collect.maps.LineDescription
import org.odk.collect.maps.MapConsts
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription

/** A polyline that can be manipulated by dragging Symbols at its vertices. */
internal class DynamicPolyLineFeature(
    private val context: Context,
    private val pointAnnotationManager: PointAnnotationManager,
    private val polylineAnnotationManager: PolylineAnnotationManager,
    private val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureDragEndListener: MapFragment.FeatureListener?,
    private val lineDescription: LineDescription
) : LineFeature {
    override val points = mutableListOf<MapPoint>()
    private val pointAnnotations = mutableListOf<PointAnnotation>()
    private val pointAnnotationClickListener = ClickListener()
    private val pointAnnotationDragListener = DragListener()
    private var polylineAnnotation: PolylineAnnotation? = null

    init {
        lineDescription.points.forEachIndexed { index, point ->
            points.add(point)

            val markerIconDescription = if (index == lineDescription.points.lastIndex) {
                MarkerIconDescription.LinePoint(lineDescription.getStrokeWidth(), MapConsts.DEFAULT_HIGHLIGHT_COLOR)
            } else {
                MarkerIconDescription.LinePoint(lineDescription.getStrokeWidth(), MapConsts.DEFAULT_STROKE_COLOR)
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
        polylineAnnotationManager.addClickListener { annotation ->
            polylineAnnotation?.let {
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

        polylineAnnotation?.let {
            polylineAnnotationManager.delete(it)
        }

        pointAnnotations.clear()
        points.clear()
    }

    private fun updateLine() {
        val points = points
            .map {
                Point.fromLngLat(it.longitude, it.latitude, it.altitude)
            }
            .toMutableList()
            .also {
                if (lineDescription.closed && it.isNotEmpty()) {
                    it.add(it.first())
                }
            }

        polylineAnnotation?.let {
            polylineAnnotationManager.delete(it)
        }

        if (points.size > 1) {
            polylineAnnotation = polylineAnnotationManager.create(
                PolylineAnnotationOptions()
                    .withPoints(points)
                    .withLineColor(lineDescription.getStrokeColor())
                    .withLineWidth(MapUtils.convertStrokeWidth(lineDescription))
            ).also {
                polylineAnnotationManager.update(it)
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
