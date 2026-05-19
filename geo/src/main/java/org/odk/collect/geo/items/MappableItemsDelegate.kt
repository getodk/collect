package org.odk.collect.geo.items

import org.odk.collect.androidshared.utils.sanitizeToColorInt
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.markers.MarkerDescription
import org.odk.collect.maps.markers.MarkerIconDescription
import org.odk.collect.maps.traces.LineDescription
import org.odk.collect.maps.traces.PolygonDescription

/**
 * Helper for showing a list of [MappableItem] objects on a [org.odk.collect.maps.MapFragment]
 */
class MappableItemsDelegate {

    /**
     * Points to be mapped. Note: kept separately from [.itemsByFeatureId] so we can
     * quickly zoom to bounding box.
     */
    private val points: MutableList<MapPoint> = mutableListOf()
    private val itemsByFeatureId: MutableMap<Int, MappableItem> = mutableMapOf()

    fun updateFeatures(map: MapFragment, items: List<MappableItem>) {
        map.clearFeatures(itemsByFeatureId.keys.toList())
        itemsByFeatureId.clear()

        val itemsAndFeatureIds = addFeatures(map, items)
        itemsAndFeatureIds.forEach { (item, featureId) ->
            itemsByFeatureId[featureId] = item
            when (item) {
                is MappableItem.Point -> points.add(item.point)
                is MappableItem.Line -> points.addAll(item.points)
                is MappableItem.Polygon -> points.addAll(item.points)
            }
        }
    }

    fun getItem(featureId: Int): MappableItem? {
        return itemsByFeatureId[featureId]
    }

    fun getFeatureId(item: MappableItem): Int? {
        return itemsByFeatureId.filter { it.value.id == item.id }.keys.firstOrNull()
    }

    fun zoomToFitItems(map: MapFragment) {
        if (points.isNotEmpty()) {
            map.zoomToBoundingBox(points, 0.8, false)
        }
    }

    private fun addFeatures(
        map: MapFragment,
        items: List<MappableItem>
    ): List<Pair<MappableItem, Int>> {
        val singlePoints = items.filterIsInstance<MappableItem.Point>()
        val lines = items.filterIsInstance<MappableItem.Line>()
        val polygons = items.filterIsInstance<MappableItem.Polygon>()

        val markerDescriptions = singlePoints.map {
            MarkerDescription(
                it.point,
                false,
                MapFragment.IconAnchor.BOTTOM,
                MarkerIconDescription.DrawableResource(it.smallIcon, it.color, it.symbol)
            )
        }

        val pointIds = map.addMarkers(markerDescriptions)
        val lineIds = mutableListOf<Int>()
        lines.forEach { item ->
            lineIds.add(
                map.addPolyLine(
                    LineDescription(
                        item.points,
                        item.strokeWidth,
                        item.strokeColor?.sanitizeToColorInt()
                    )
                )
            )
        }

        val polygonIds = mutableListOf<Int>()
        polygons.forEach { item ->
            polygonIds.add(
                map.addPolygon(
                    PolygonDescription(
                        item.points,
                        item.strokeWidth,
                        item.strokeColor?.sanitizeToColorInt(),
                        item.fillColor?.sanitizeToColorInt()
                    )
                )
            )
        }

        return (singlePoints + lines + polygons).zip(pointIds + lineIds + polygonIds)
    }
}