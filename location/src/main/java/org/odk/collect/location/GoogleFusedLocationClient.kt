package org.odk.collect.location

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.odk.collect.location.LocationClient.LocationClientListener
import org.odk.collect.location.LocationUtils.sanitizeAccuracy

/**
 * An implementation of [LocationClient] that uses Google Play Services to retrieve the User's location.
 *
 * Should be used whenever Google Play Services is present. In general, use
 * [LocationClientProvider] to retrieve a configured [LocationClient].
 */
class GoogleFusedLocationClient(
    private val fusedLocationProviderClientWrapper: FusedLocationProviderClientWrapper,
    locationManager: LocationManager
) : BaseLocationClient(locationManager), LocationListener {

    constructor(application: Application) : this(
        FusedLocationProviderClientWrapper(LocationServices.getFusedLocationProviderClient(application)),
        (application.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
    )

    private var lastKnownLocation: Location? = null
        set(value) {
            field = sanitizeAccuracy(value, retainMockAccuracy)
        }

    private var locationListener: LocationListener? = null
    private var updateInterval: Long = 5000
    private var fastestUpdateInterval: Long = 2500
    private var retainMockAccuracy = false

    override fun start(listener: LocationClientListener) {
        fusedLocationProviderClientWrapper.start(this)
        setListener(listener)
        listener.onClientStart()
    }

    override fun stop() {
        stopLocationUpdates()
        getListener()?.onClientStop()
        setListener(null)
    }

    override fun requestLocationUpdates(locationListener: LocationListener) {
        if (!isMonitoringLocation) {
            fusedLocationProviderClientWrapper.requestLocationUpdates(createLocationRequest())
        }
        this.locationListener = locationListener
    }

    override fun stopLocationUpdates() {
        if (isMonitoringLocation) {
            fusedLocationProviderClientWrapper.removeLocationUpdates()
            locationListener = null
        }
    }

    override fun setRetainMockAccuracy(retainMockAccuracy: Boolean) {
        this.retainMockAccuracy = retainMockAccuracy
    }

    override fun getLastLocation(): Location? {
        return lastKnownLocation
    }

    override fun isMonitoringLocation(): Boolean {
        return locationListener != null
    }

    override fun setUpdateIntervals(updateInterval: Long, fastestUpdateInterval: Long) {
        this.updateInterval = updateInterval
        this.fastestUpdateInterval = fastestUpdateInterval
    }

    override fun onLocationChanged(location: Location) {
        lastKnownLocation = location
        lastKnownLocation?.let {
            locationListener?.onLocationChanged(it)
        }
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            priority = this@GoogleFusedLocationClient.getPriority().value
            interval = updateInterval
            fastestInterval = fastestUpdateInterval
        }
    }
}

@SuppressLint("MissingPermission") // Permission checks for location services handled in components that use this class
class FusedLocationProviderClientWrapper(private val fusedLocationProviderClient: FusedLocationProviderClient) {
    private lateinit var listener: LocationListener

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                listener.onLocationChanged(location)
            }
        }
    }

    fun start(listener: LocationListener) {
        this.listener = listener
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                listener.onLocationChanged(it)
            }
        }
    }

    fun requestLocationUpdates(locationRequest: LocationRequest) {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}
