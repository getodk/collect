package org.odk.collect.location.satellites

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.location.GpsStatus
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import org.odk.collect.androidshared.livedata.NonNullLiveData

class GpsStatusSatelliteInfoClient(private val locationManager: LocationManager) :
    SatelliteInfoClient {

    override val satellitesUsedInLastFix: NonNullLiveData<Int>
        get() {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                GpsStatusSatellitesLiveData(locationManager)
            } else {
                GnssStatusSatellitesLiveData(locationManager)
            }
        }
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

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.N)
private class GnssStatusSatellitesLiveData(private val locationManager: LocationManager) :
    NonNullLiveData<Int>(0) {

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            super.onSatelliteStatusChanged(status)
            var usedInFix = 0
            for (index in 0 until status.satelliteCount) {
                if (status.usedInFix(index)) {
                    usedInFix++
                }
            }
            value = usedInFix
        }
    }

    override fun onActive() {
        locationManager.registerGnssStatusCallback(gnssStatusCallback, null)
    }

    override fun onInactive() {
        locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
    }
}
