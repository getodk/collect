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
import org.odk.collect.maps.MapConsts.MAPBOX_POLYLINE_STROKE_WIDTH
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint

/** A polyline that can be manipulated by dragging Symbols at its vertices. */
internal class DynamicPolyLineFeature(
    private val context: Context,
    private val pointAnnotationManager: PointAnnotationManager,
    private val polylineAnnotationManager: PolylineAnnotationManager,
    private val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureDragEndListener: MapFragment.FeatureListener?,
    private val lineDescription: LineDescription
) : MapFeature {
    val mapPoints = mutableListOf<MapPoint>()
    private val pointAnnotations = mutableListOf<PointAnnotation>()
    private val pointAnnotationClickListener = ClickListener()
    private val pointAnnotationDragListener = DragListener()
    private var polylineAnnotation: PolylineAnnotation? = null

    init {
        lineDescription.points.forEach {
            mapPoints.add(it)
            pointAnnotations.add(
                MapUtils.createPointAnnotation(
                    pointAnnotationManager,
                    it,
                    true,
                    MapFragment.CENTER,
                    org.odk.collect.icons.R.drawable.ic_map_point,
                    context
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
        mapPoints.clear()
    }

    fun appendPoint(point: MapPoint) {
        mapPoints.add(point)
        pointAnnotations.add(
            MapUtils.createPointAnnotation(
                pointAnnotationManager,
                point,
                true,
                MapFragment.CENTER,
                org.odk.collect.icons.R.drawable.ic_map_point,
                context
            )
        )
        updateLine()
    }

    fun removeLastPoint() {
        if (pointAnnotations.isNotEmpty()) {
            pointAnnotationManager.delete(pointAnnotations.last())
            pointAnnotations.removeLast()
            mapPoints.removeLast()
            updateLine()
        }
    }

    private fun updateLine() {
        val points = mapPoints
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
                    .withLineWidth(MAPBOX_POLYLINE_STROKE_WIDTH.toDouble())
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
                    mapPoints[index] = MapUtils.mapPointFromPointAnnotation(pointAnnotation)
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
