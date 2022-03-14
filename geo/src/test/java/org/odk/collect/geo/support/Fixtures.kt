package org.odk.collect.geo.support

import android.R
import org.odk.collect.geo.MappableSelectItem

object Fixtures {
    fun mappableSelectItem(): MappableSelectItem {
        return MappableSelectItem(
            0,
            0.0,
            0.0,
            R.drawable.ic_lock_power_off,
            R.drawable.ic_lock_idle_charging,
            "0",
            MappableSelectItem.IconifiedText(R.drawable.ic_lock_idle_charging, "An item"),
            null,
            null
        )
    }
}
