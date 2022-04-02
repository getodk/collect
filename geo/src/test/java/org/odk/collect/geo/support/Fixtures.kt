package org.odk.collect.geo.support

import android.R
import org.odk.collect.geo.selection.MappableSelectItem

object Fixtures {
    fun actionMappableSelectItem(): MappableSelectItem.WithAction {
        return MappableSelectItem.WithAction(
            0,
            0.0,
            0.0,
            R.drawable.ic_lock_power_off,
            R.drawable.ic_lock_idle_charging,
            "0",
            listOf(MappableSelectItem.IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            MappableSelectItem.IconifiedText(R.drawable.ic_delete, "Action"),
        )
    }

    fun infoMappableSelectItem(): MappableSelectItem.WithInfo {
        return MappableSelectItem.WithInfo(
            0,
            0.0,
            0.0,
            R.drawable.ic_lock_power_off,
            R.drawable.ic_lock_idle_charging,
            "0",
            listOf(MappableSelectItem.IconifiedText(R.drawable.ic_lock_idle_charging, "An item")),
            "Info"
        )
    }
}
