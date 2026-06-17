package org.odk.collect.mapbox

import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import org.odk.collect.maps.circles.CircleDescription
import org.odk.collect.maps.circles.getFillColor
import org.odk.collect.maps.circles.getStrokeColor

class CircleFeature(
    private val mapboxMap: MapboxMap,
    private val circleAnnotationManager: CircleAnnotationManager,
    private val circleDescription: CircleDescription
) : MapFeature {

    private var lastRenderedZoom = mapboxMap.cameraState.zoom

    private val circleAnnotation = circleAnnotationManager.create(
        CircleAnnotationOptions()
            .withPoint(
                Point.fromLngLat(
                    circleDescription.center.longitude,
                    circleDescription.center.latitude
                )
            )
            .withCircleRadius(pixelRadius(lastRenderedZoom))
            .withCircleColor(circleDescription.getFillColor())
            .withCircleStrokeColor(circleDescription.getStrokeColor())
            .withCircleStrokeWidth(1.0)
    )

    // A CircleAnnotation's radius is in screen pixels, so it has to be recomputed from metres
    // whenever the zoom changes (including the initial zoom to the current location).
    private val cameraChangeListener = OnCameraChangeListener { refresh() }.also {
        mapboxMap.addOnCameraChangeListener(it)
    }

    override fun dispose() {
        mapboxMap.removeOnCameraChangeListener(cameraChangeListener)
        circleAnnotationManager.delete(circleAnnotation)
    }

    private fun refresh() {
        val zoom = mapboxMap.cameraState.zoom
        if (zoom == lastRenderedZoom) {
            return
        }
        lastRenderedZoom = zoom
        circleAnnotation.circleRadius = pixelRadius(zoom)
        circleAnnotationManager.update(circleAnnotation)
    }

    private fun pixelRadius(zoom: Double): Double {
        // CircleAnnotation's radius is in screen pixels but CircleDescription.radius is in metres, so
        // convert using Mapbox's own metres-per-pixel for the circle's latitude at the given zoom.
        val metersPerPixel =
            mapboxMap.getMetersPerPixelAtLatitude(circleDescription.center.latitude, zoom)
        return circleDescription.radius.toDouble() / metersPerPixel
    }
}
