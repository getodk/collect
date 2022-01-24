package org.odk.collect.location

import android.location.Location

object LocationUtils {

    @JvmStatic
    @JvmOverloads
    fun sanitizeAccuracy(location: Location?, retainMockAccuracy: Boolean = false): Location? {
        if (location != null && (location.isFromMockProvider && !retainMockAccuracy || location.accuracy < 0)) {
            location.accuracy = 0f
        }

        return location
    }
}
