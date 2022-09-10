package org.odk.collect.mapbox

import android.content.Context
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconCreator
import org.odk.collect.maps.markers.MarkerIconDescription

object MapUtils {
    fun createPointAnnotation(
        pointAnnotationManager: PointAnnotationManager,
        point: MapPoint,
        draggable: Boolean,
        @MapFragment.IconAnchor iconAnchor: String,
        iconDrawableId: Int,
        context: Context
    ): PointAnnotation {
        return pointAnnotationManager.create(
            PointAnnotationOptions()
                .withPoint(Point.fromLngLat(point.lon, point.lat, point.alt))
                .withIconImage(MarkerIconCreator.getMarkerIconBitmap(context, MarkerIconDescription(iconDrawableId)))
                .withIconSize(1.0)
                .withSymbolSortKey(10.0)
                .withDraggable(draggable)
                .withTextOpacity(0.0)
                .withIconAnchor(getIconAnchorValue(iconAnchor))
        )
    }

    fun createPointAnnotations(
        context: Context,
        pointAnnotationManager: PointAnnotationManager,
        markerFeatures: List<MarkerDescription>,
    ): List<PointAnnotation> {
        val pointAnnotationOptionsList = markerFeatures.map {
            PointAnnotationOptions()
                .withPoint(Point.fromLngLat(it.point.lon, it.point.lat, it.point.alt))
                .withIconImage(MarkerIconCreator.getMarkerIconBitmap(context, it.iconDescription))
                .withIconSize(1.0)
                .withSymbolSortKey(10.0)
                .withDraggable(it.isDraggable)
                .withTextOpacity(0.0)
                .withIconAnchor(getIconAnchorValue(it.iconAnchor))
        }

        return pointAnnotationManager.create(pointAnnotationOptionsList)
    }

    private fun getIconAnchorValue(@MapFragment.IconAnchor iconAnchor: String): IconAnchor {
        return when (iconAnchor) {
            MapFragment.BOTTOM -> IconAnchor.BOTTOM
            else -> IconAnchor.CENTER
        }
    }

    fun mapPointFromPointAnnotation(pointAnnotation: PointAnnotation): MapPoint {
        // When a symbol is manually dragged, the position is no longer
        // obtained from a GPS reading, so the altitude and standard
        // deviation fields are no longer meaningful; reset them to zero.
        return MapPoint(pointAnnotation.point.latitude(), pointAnnotation.point.longitude(), 0.0, 0.0)
    }
}
