package org.odk.collect.location

import android.location.LocationManager
import org.odk.collect.location.LocationClient.LocationClientListener
import java.lang.ref.WeakReference

/**
 * An abstract base LocationClient class that provides some shared functionality for determining
 * whether or not certain Location providers are available.
 *
 * Constructs a new BaseLocationClient with the provided LocationManager.
 * This Constructor is only accessible to child classes.
 *
 * @param locationManager The LocationManager to retrieve locations from.
 */
internal abstract class BaseLocationClient(protected val locationManager: LocationManager?) :
    LocationClient {

    private var listenerRef: WeakReference<LocationClientListener?>? = null
    private var priority = LocationClient.Priority.PRIORITY_HIGH_ACCURACY

    override fun isLocationAvailable(): Boolean {
        return getProvider() != null
    }

    protected fun getProvider(): String? {
        var provider = LocationManager.PASSIVE_PROVIDER
        var backupProvider: String? = null

        when (priority) {
            LocationClient.Priority.PRIORITY_HIGH_ACCURACY -> {
                provider = LocationManager.GPS_PROVIDER
                backupProvider = LocationManager.NETWORK_PROVIDER
            }
            LocationClient.Priority.PRIORITY_BALANCED_POWER_ACCURACY -> {
                provider = LocationManager.NETWORK_PROVIDER
                backupProvider = LocationManager.GPS_PROVIDER
            }
            LocationClient.Priority.PRIORITY_LOW_POWER -> {
                provider = LocationManager.NETWORK_PROVIDER
                backupProvider = LocationManager.PASSIVE_PROVIDER
            }
            LocationClient.Priority.PRIORITY_NO_POWER -> {
                provider = LocationManager.PASSIVE_PROVIDER
                backupProvider = null
            }
        }

        return getProviderIfEnabled(provider, backupProvider)
    }

    private fun getProviderIfEnabled(provider: String, backupProvider: String?): String? {
        if (hasProvider(provider)) {
            return provider
        } else if (hasProvider(backupProvider)) {
            return backupProvider
        }
        return null
    }

    private fun hasProvider(provider: String?): Boolean {
        if (provider == null) {
            return false
        }

        val enabledProviders = locationManager!!.getProviders(true)
        for (enabledProvider in enabledProviders) {
            if (enabledProvider.equals(provider, ignoreCase = true)) {
                return true
            }
        }

        return false
    }

    override fun setPriority(priority: LocationClient.Priority) {
        this.priority = priority
    }

    protected fun getPriority(): LocationClient.Priority {
        return priority
    }

    override fun setListener(locationClientListener: LocationClientListener?) {
        listenerRef = WeakReference(locationClientListener)
    }

    protected fun getListener(): LocationClientListener? {
        return if (listenerRef != null) listenerRef!!.get() else null
    }
}
