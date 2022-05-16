package org.odk.collect.android.geo.mapboxsdk

import android.content.Context
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.utils.ColorUtils
import com.mapbox.maps.plugin.annotation.Annotation
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import org.odk.collect.android.R
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint

/** A polyline or polygon that can be manipulated by dragging Symbols at its vertices. */
internal class PolyFeature(
    private val context: Context,
    private val pointAnnotationManager: PointAnnotationManager,
    private val polylineAnnotationManager: PolylineAnnotationManager,
    private val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val featureDragEndListener: MapFragment.FeatureListener?,
    private val closedPolygon: Boolean,
    initMapPoints: Iterable<MapPoint>
) : MapFeature {
    val mapPoints = mutableListOf<MapPoint>()
    private val pointAnnotations = mutableListOf<PointAnnotation>()
    private val pointAnnotationClickListener = ClickListener()
    private val pointAnnotationDragListener = DragListener()
    private var polylineAnnotation: PolylineAnnotation

    init {
        initMapPoints.forEach {
            mapPoints.add(it)
            pointAnnotations.add(
                MapUtils.createPointAnnotation(
                    pointAnnotationManager,
                    it,
                    true,
                    MapFragment.CENTER,
                    R.drawable.ic_map_point,
                    context
                )
            )
        }

        polylineAnnotation = polylineAnnotationManager.create(
            PolylineAnnotationOptions()
                .withPoints(emptyList())
                .withLineColor(ColorUtils.colorToRgbaString(context.resources.getColor(R.color.mapLineColor)))
                .withLineWidth(5.0)
        )

        updateLine()

        pointAnnotationManager.addClickListener(pointAnnotationClickListener)
        pointAnnotationManager.addDragListener(pointAnnotationDragListener)
    }

    override fun dispose() {
        pointAnnotationManager.apply {
            removeClickListener(pointAnnotationClickListener)
            removeDragListener(pointAnnotationDragListener)
            delete(pointAnnotations)
        }

        polylineAnnotationManager.apply {
            delete(polylineAnnotation)
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
                R.drawable.ic_map_point,
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
        polylineAnnotation.points = mapPoints
            .map {
                Point.fromLngLat(it.lon, it.lat, it.alt)
            }
            .toMutableList()
            .also {
                if (closedPolygon && it.isNotEmpty()) {
                    it.add(it.first())
                }
            }
        polylineAnnotationManager.update(polylineAnnotation)
    }

    private inner class ClickListener : OnPointAnnotationClickListener {
        override fun onAnnotationClick(annotation: PointAnnotation): Boolean {
            for (pointAnnotation in pointAnnotations) {
                if (annotation.id == pointAnnotation.id && featureClickListener != null) {
                    featureClickListener.onFeature(featureId)
                    break
                }
            }
            return true
        }
    }

    private inner class DragListener : OnPointAnnotationDragListener {
        override fun onAnnotationDragStarted(annotation: Annotation<*>) = Unit

        override fun onAnnotationDrag(annotation: Annotation<*>) {
            pointAnnotations.forEachIndexed { index, pointAnnotation ->
                if (annotation.id == pointAnnotation.id) {
                    mapPoints[index] = MapUtils.mapPointFromPointAnnotation(pointAnnotation)
                }
            }
            updateLine()
        }

        override fun onAnnotationDragFinished(annotation: Annotation<*>) {
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
