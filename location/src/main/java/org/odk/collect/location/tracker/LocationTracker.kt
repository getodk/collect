package org.odk.collect.location.tracker

import org.odk.collect.location.Location

/**
 * Provides a way to track the location of a device.
 */
interface LocationTracker {

    /**
     * The last location tracked by this [LocationTracker].
     */
    fun getCurrentLocation(): Location?

    /**
     * Starts tracking the device's location at a fixed interval (5s)
     */
    fun start()

    fun stop()
}
