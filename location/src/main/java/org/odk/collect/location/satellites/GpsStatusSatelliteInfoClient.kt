package org.odk.collect.location

import android.annotation.SuppressLint
import android.location.GpsStatus
import android.location.LocationManager
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.location.satellites.SatelliteInfoClient

class GpsStatusSatelliteInfoClient(private val locationManager: LocationManager) :
    SatelliteInfoClient {

    override val satellitesUsedInLastFix: NonNullLiveData<Int>
        get() = GpsStatusSatellitesLiveData(locationManager)
}

@SuppressLint("MissingPermission")
@Suppress("deprecation")
private class GpsStatusSatellitesLiveData(private val locationManager: LocationManager) :
    NonNullLiveData<Int>(0),
    GpsStatus.Listener {

    override fun onActive() {
        locationManager.addGpsStatusListener(this)
    }

    override fun onInactive() {
        locationManager.removeGpsStatusListener(this)
    }

    override fun onGpsStatusChanged(event: Int) {
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            val status = locationManager.getGpsStatus(null)
            val usedInFix = status?.satellites?.filter { it.usedInFix() }?.count() ?: 0
            value = usedInFix
        }
    }
}
