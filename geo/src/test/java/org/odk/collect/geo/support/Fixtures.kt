package org.odk.collect.geo.support

import android.R
import org.odk.collect.geo.selection.IconifiedText
import org.odk.collect.geo.selection.MappableSelectItem
import org.odk.collect.maps.MapPoint

object Fixtures {
    fun actionMappableSelectItem(): MappableSelectItem {
        return MappableSelectItem(
            0,
            listOf(MapPoint(0.0, 0.0)),
            R.drawable.ic_lock_power_off,
            R.drawable.ic_lock_idle_charging,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            action = IconifiedText(R.drawable.ic_delete, "Action")
        )
    }

    fun infoMappableSelectItem(): MappableSelectItem {
        return MappableSelectItem(
            0,
            listOf(MapPoint(0.0, 0.0)),
            R.drawable.ic_lock_power_off,
            R.drawable.ic_lock_idle_charging,
            "0",
            listOf(IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            info = "Info"
        )
    }
}
