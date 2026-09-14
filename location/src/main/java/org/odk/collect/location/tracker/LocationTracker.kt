package org.odk.collect.location.tracker

import android.app.ActivityManager
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
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
}

fun LocationTracker.getCurrentLocation(): Location? {
    return this.getLocation().value
}

fun LocationTracker.bindToLifecycle(
    fragment: Fragment,
    retainMockAccuracy: Boolean = false
) {
    fragment.bindToForeground(
        onForeground = {
            start(
                retainMockAccuracy = retainMockAccuracy,
                updateInterval = null,
                notification = false
            )
        },
        onBackground = {
            stop()
        }
    )
}

fun Fragment.bindToForeground(onForeground: () -> Unit, onBackground: () -> Unit) {
    val delayedCheckScope = CoroutineScope(Dispatchers.Main)

    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            delayedCheckScope.launch {
                while (!isAppInForeground(requireContext())) {
                    delay(100.milliseconds)
                }

                onForeground()
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            delayedCheckScope.cancel()
            onBackground()
        }
    })
}

private fun isAppInForeground(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appProcesses = activityManager.runningAppProcesses ?: return false

    val packageName = context.packageName
    for (appProcess in appProcesses) {
        if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
            appProcess.processName == packageName) {
            return true
        }
    }

    return false
}
