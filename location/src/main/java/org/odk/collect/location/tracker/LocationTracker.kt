package org.odk.collect.location.tracker

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
    fun start(
        retainMockAccuracy: Boolean = false,
        updateInterval: Long? = null,
        background: Boolean = true
    )

    /**
     * Stops tracking location. Resets the value returned by [LocationTracker.getCurrentLocation].
     */
    fun stop()
}

fun LocationTracker.getCurrentLocation(): Location? {
    return this.getLocation().value
}

fun LocationTracker.bindToLifecycle(
    lifecycle: LifecycleOwner,
    retainMockAccuracy: Boolean
) {
    lifecycle.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            start(
                retainMockAccuracy = retainMockAccuracy,
                updateInterval = null,
                background = false
            )
        }

        override fun onPause(owner: LifecycleOwner) {
            stop()
        }
    })
}
