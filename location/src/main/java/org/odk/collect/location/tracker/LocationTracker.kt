package org.odk.collect.location.tracker

import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.location.Location

/**
 * Provides a way to track the location of a device.
 */
interface LocationTracker {

    /**
     * Will be `null` if a location hasn't been determined or [LocationTracker.start] hasn't been
     * called yet.
     */
    fun getLocation(): StateFlow<Location?>

    /**
     * @param updateInterval requested (not guaranteed) interval for location updates
     */
    fun start(retainMockAccuracy: Boolean, updateInterval: Long? = null)
    fun start(retainMockAccuracy: Boolean) = start(retainMockAccuracy, null)
    fun start(updateInterval: Long?) = start(false, updateInterval)
    fun start() = start(false, null)

    /**
     * Stops tracking location. Does not reset the value returned by [LocationTracker.getCurrentLocation].
     */
    fun stop()
}

fun LocationTracker.getCurrentLocation(): Location? {
    return this.getLocation().value
}
