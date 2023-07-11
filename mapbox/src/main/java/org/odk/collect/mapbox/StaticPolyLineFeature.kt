package org.odk.collect.mapbox

import android.content.Context
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.utils.ColorUtils
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import org.odk.collect.maps.MapConsts.MAPBOX_POLYLINE_STROKE_WIDTH
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint

/** A polyline that can not be manipulated by dragging Symbols at its vertices. */
internal class StaticPolyLineFeature(
    context: Context,
    private val polylineAnnotationManager: PolylineAnnotationManager,
    private val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val closedPolygon: Boolean,
    initMapPoints: Iterable<MapPoint>
) : MapFeature {
    private val mapPoints = mutableListOf<MapPoint>()
    private var polylineAnnotation: PolylineAnnotation? = null

    init {
        initMapPoints.forEach {
            mapPoints.add(it)
        }

        val points = mapPoints
            .map {
                Point.fromLngLat(it.longitude, it.latitude, it.altitude)
            }
            .toMutableList()
            .also {
                if (closedPolygon && it.isNotEmpty()) {
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
                    .withLineColor(ColorUtils.colorToRgbaString(context.resources.getColor(org.odk.collect.icons.R.color.mapLineColor)))
                    .withLineWidth(MAPBOX_POLYLINE_STROKE_WIDTH.toDouble())
            ).also {
                polylineAnnotationManager.update(it)
            }
        }

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
        polylineAnnotation?.let {
            polylineAnnotationManager.delete(it)
        }
        mapPoints.clear()
    }
}
