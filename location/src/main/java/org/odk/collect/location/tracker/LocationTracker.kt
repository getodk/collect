package org.odk.collect.location.tracker

import android.app.ActivityManager
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.odk.collect.location.Location
import kotlin.time.Duration.Companion.milliseconds

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
        retainMockAccuracy: Boolean,
        updateInterval: Long? = null,
        notification: Boolean = true
    )

    fun start(retainMockAccuracy: Boolean) = start(retainMockAccuracy, null)
    fun start(updateInterval: Long?) = start(false, updateInterval)
    fun start() = start(false, null)

    /**
     * Stops tracking location. Does not reset the value returned by [LocationTracker.getCurrentLocation].
     */
    fun stop()

    fun bindToLifecycle(
        context: Context,
        lifecycle: LifecycleOwner,
        retainMockAccuracy: Boolean = false
    )
}

fun LocationTracker.getCurrentLocation(): Location? {
    return this.getLocation().value
}
