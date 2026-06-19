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
            .withCircleRadius(
                mapboxMap.convertMetersToPixels(
                    circleDescription.radius.toDouble(),
                    circleDescription.center.latitude,
                    lastRenderedZoom
                )
            )
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
        circleAnnotation.circleRadius = mapboxMap.convertMetersToPixels(
            circleDescription.radius.toDouble(),
            circleDescription.center.latitude,
            zoom
        )
        circleAnnotationManager.update(circleAnnotation)
    }
}

private fun MapboxMap.convertMetersToPixels(meters: Double, latitude: Double, zoom: Double): Double {
    val metersPerPixel = getMetersPerPixelAtLatitude(latitude, zoom)
    return meters / metersPerPixel
}
