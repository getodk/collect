package org.odk.collect.android.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

interface LocationProvider {
    fun getLastLocation(): Location?
}

class SystemLocationProvider(
    private val context: Context
) : LocationProvider {

    companion object {
        private const val PROVIDER_FUSED = "fused";
    }

    override fun getLastLocation(): Location? {
        val permission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        return if (permission == PackageManager.PERMISSION_GRANTED) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.getLastKnownLocation(PROVIDER_FUSED)
        } else null
    }

}