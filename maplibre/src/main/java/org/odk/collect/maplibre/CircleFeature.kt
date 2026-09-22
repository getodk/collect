package org.odk.collect.maplibre

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.CircleOptions
import org.maplibre.android.utils.ColorUtils
import org.odk.collect.maps.circles.CircleDescription
import org.odk.collect.maps.circles.getFillColor
import org.odk.collect.maps.circles.getStrokeColor

class CircleFeature(
    private val map: MapLibreMap,
    private val circleManager: CircleManager,
    private val circleDescription: CircleDescription
) : MapFeature {

    private var lastRenderedZoom = map.cameraPosition.zoom

    private val circle = circleManager.create(
        CircleOptions()
            .withLatLng(
                LatLng(
                    circleDescription.center.latitude,
                    circleDescription.center.longitude
                )
            )
            .withCircleRadius(
                map.convertMetersToPixels(
                    circleDescription.radius.toDouble(),
                    circleDescription.center.latitude
                )
            )
            .withCircleColor(ColorUtils.colorToRgbaString(circleDescription.getFillColor()))
            .withCircleStrokeColor(ColorUtils.colorToRgbaString(circleDescription.getStrokeColor()))
            .withCircleStrokeWidth(1.0f)
    )

    // A Circle's radius is in screen pixels, so it has to be recomputed from metres
    // whenever the zoom changes (including the initial zoom to the current location).
    private val cameraMoveListener = MapLibreMap.OnCameraMoveListener { refresh() }.also {
        map.addOnCameraMoveListener(it)
    }

    override fun dispose() {
        map.removeOnCameraMoveListener(cameraMoveListener)
        circleManager.delete(circle)
    }

    private fun refresh() {
        val zoom = map.cameraPosition.zoom
        if (zoom == lastRenderedZoom) {
            return
        }
        lastRenderedZoom = zoom
        circle.circleRadius = map.convertMetersToPixels(
            circleDescription.radius.toDouble(),
            circleDescription.center.latitude
        )
        circleManager.update(circle)
    }
}

private fun MapLibreMap.convertMetersToPixels(meters: Double, latitude: Double): Float {
    val metersPerPixel = projection.getMetersPerPixelAtLatitude(latitude)
    return (meters / metersPerPixel).toFloat()
}
