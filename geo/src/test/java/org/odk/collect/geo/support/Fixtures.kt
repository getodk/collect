package org.odk.collect.geo.support

import android.R
import org.odk.collect.geo.items.IconifiedText
import org.odk.collect.geo.items.MappableItem
import org.odk.collect.maps.MapPoint

object Fixtures {
    fun actionMappableSelectPoint(): MappableItem.MappablePoint {
        return MappableItem.MappablePoint(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            point = MapPoint(0.0, 0.0),
            smallIcon = R.drawable.ic_lock_power_off,
            largeIcon = R.drawable.ic_lock_idle_charging,
            action = IconifiedText(R.drawable.ic_delete, "Action")
        )
    }

    fun infoMappableSelectPoint(): MappableItem.MappablePoint {
        return MappableItem.MappablePoint(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            point = MapPoint(0.0, 0.0),
            smallIcon = R.drawable.ic_lock_power_off,
            largeIcon = R.drawable.ic_lock_idle_charging,
            info = "Info"
        )
    }

    fun actionMappableSelectLine(): MappableItem.MappableLine {
        return MappableItem.MappableLine(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            points = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0)),
            action = IconifiedText(R.drawable.ic_delete, "Action")
        )
    }

    fun actionMappableSelectPolygon(): MappableItem.MappablePolygon {
        return MappableItem.MappablePolygon(
            0,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            points = listOf(MapPoint(0.0, 0.0), MapPoint(1.0, 1.0)),
            action = IconifiedText(R.drawable.ic_delete, "Action")
        )
    }
}
