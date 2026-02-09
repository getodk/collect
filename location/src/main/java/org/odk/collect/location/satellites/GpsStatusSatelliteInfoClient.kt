package org.odk.collect.location.satellites

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.location.LocationManager
import org.odk.collect.androidshared.livedata.NonNullLiveData

class GpsStatusSatelliteInfoClient(private val locationManager: LocationManager) :
    SatelliteInfoClient {

    override val satellitesUsedInLastFix: NonNullLiveData<Int>
        get() {
            return GnssStatusSatellitesLiveData(locationManager)
        }
}

@SuppressLint("MissingPermission")
private class GnssStatusSatellitesLiveData(private val locationManager: LocationManager) :
    NonNullLiveData<Int>(0) {

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            value = (0 until status.satelliteCount).fold(0) { count, index ->
                when {
                    status.usedInFix(index) -> count + 1
                    else -> count
                }
            }
        }
    }

    override fun onActive() {
        locationManager.registerGnssStatusCallback(gnssStatusCallback, null)
    }

    override fun onInactive() {
        locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
    }
}
