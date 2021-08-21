package org.odk.collect.location

import android.location.Location

object LocationUtils {

    @JvmStatic
    fun sanitizeAccuracy(location: Location?): Location? {
        if (location != null && (location.isFromMockProvider || location.accuracy < 0)) {
            location.accuracy = 0f
        }
        return location
    }
}
