package org.odk.collect.location

import android.annotation.SuppressLint
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
 * An implementation of [LocationClient] that uses Google Play Services to retrieve the user's location.
 *
 * Should be used whenever Google Play Services is present. In general, use
 * [LocationClientProvider] to retrieve a configured [LocationClient].
 */
class GoogleFusedLocationClient(
    private val fusedLocationProviderClientWrapper: FusedLocationProviderClientWrapper,
    locationManager: LocationManager
) : BaseLocationClient(locationManager), LocationListener {

    constructor(context: Context) : this(
        FusedLocationProviderClientWrapper(LocationServices.getFusedLocationProviderClient(context)),
        (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
    )

    private var locationListener: LocationListener? = null
    private var updateInterval = DEFAULT_UPDATE_INTERVAL
    private var fastestUpdateInterval = DEFAULT_FASTEST_UPDATE_INTERVAL
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
        return sanitizeAccuracy(fusedLocationProviderClientWrapper.getLastLocation(), retainMockAccuracy)
    }

    override fun isMonitoringLocation(): Boolean {
        return locationListener != null
    }

    override fun setUpdateIntervals(updateInterval: Long, fastestUpdateInterval: Long) {
        this.updateInterval = updateInterval
        this.fastestUpdateInterval = fastestUpdateInterval
    }

    override fun onLocationChanged(location: Location) {
        sanitizeAccuracy(location, retainMockAccuracy)?.let {
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

    companion object {
        /**
         * The default requested time between location updates, in milliseconds.
         */
        private const val DEFAULT_UPDATE_INTERVAL: Long = 5000

        /**
         * The default maximum rate at which location updates can arrive (other updates will be throttled),
         * in milliseconds.
         */
        private const val DEFAULT_FASTEST_UPDATE_INTERVAL: Long = 2500
    }
}

@SuppressLint("MissingPermission") // Permission checks for location services handled in components that use this class
class FusedLocationProviderClientWrapper(private val fusedLocationProviderClient: FusedLocationProviderClient) {
    private lateinit var listener: LocationListener
    private var lastLocation: Location? = null

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                listener.onLocationChanged(location)
            }
        }
    }

    fun start(listener: LocationListener) {
        this.listener = listener
        fusedLocationProviderClient
            .lastLocation
            .addOnSuccessListener { location: Location? ->
                lastLocation = location
            }
    }

    fun requestLocationUpdates(locationRequest: LocationRequest) {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    fun getLastLocation(): Location? {
        return lastLocation
    }
}
