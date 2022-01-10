package org.odk.collect.location.tracker

import org.odk.collect.location.Location

/**
 * Provides a way to track the location of a device.
 */
interface LocationTracker {

    /**
     * The last location tracked. Will return `null` if a location hasn't been determined
     * or [LocationTracker.start] hasn't been called yet.
     */
    fun getCurrentLocation(): Location?

    fun start(retainMockAccuracy: Boolean, updateInterval: Long? = null)
    fun start(retainMockAccuracy: Boolean) = start(retainMockAccuracy, null)
    fun start(updateInterval: Long?) = start(false, updateInterval)
    fun start() = start(false, null)

    /**
     * Stops tracking location. Does not reset the value returned by [LocationTracker.getCurrentLocation].
     */
    fun stop()
}
