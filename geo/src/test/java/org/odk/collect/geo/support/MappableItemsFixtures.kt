package org.odk.collect.geo.support

import android.R
import org.odk.collect.geo.items.IconifiedText
import org.odk.collect.geo.items.MappableItem
import org.odk.collect.maps.MapPoint

object MappableItemsFixtures {
    fun actionMappablePoint(): MappableItem.Point {
        return MappableItem.Point(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            point = MapPoint(0.0, 0.0),
            smallIcon = R.drawable.ic_lock_power_off,
            largeIcon = R.drawable.ic_lock_idle_charging,
            action = IconifiedText(R.drawable.ic_delete, "Action")
        )
    }

    fun point(point: MapPoint = MapPoint(0.0, 0.0), info: String? = null): MappableItem.Point {
        return MappableItem.Point(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            point = point,
            smallIcon = R.drawable.ic_lock_power_off,
            largeIcon = R.drawable.ic_lock_idle_charging,
            info = info
        )
    }

    fun actionMappableLine(): MappableItem.Line {
        return MappableItem.Line(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            points = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0)),
            action = IconifiedText(R.drawable.ic_delete, "Action")
        )
    }

    fun actionMappablePolygon(): MappableItem.Polygon {
        return MappableItem.Polygon(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            points = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0)),
            action = IconifiedText(R.drawable.ic_delete, "Action")
        )
    }
}
