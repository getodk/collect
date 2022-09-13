package org.odk.collect.permissions

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat

internal interface LocationAccessibilityChecker {
    fun isLocationEnabled(activity: Activity): Boolean
}

internal object LocationAccessibilityCheckerImpl : LocationAccessibilityChecker {
    override fun isLocationEnabled(activity: Activity): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }
}
