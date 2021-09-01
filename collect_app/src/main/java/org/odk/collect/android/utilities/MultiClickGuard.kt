package org.odk.collect.android.utilities

import android.os.SystemClock

object MultiClickGuard {
    private const val CLICK_DEBOUNCE_MS = 1000

    @JvmField
    var test = false

    private var lastClickTime: Long = 0
    private var lastClickName: String = javaClass.name

    // Debounce multiple clicks within the same screen
    @JvmStatic
    fun allowClick(className: String = javaClass.name): Boolean {
        if (test) {
            return true
        }
        val elapsedRealtime = SystemClock.elapsedRealtime()
        val isSameClass = className == lastClickName
        val isBeyondThreshold = elapsedRealtime - lastClickTime > CLICK_DEBOUNCE_MS
        val isBeyondTestThreshold =
            lastClickTime == 0L || lastClickTime == elapsedRealtime // just for tests

        val allowClick = !isSameClass || isBeyondThreshold || isBeyondTestThreshold

        if (allowClick) {
            lastClickTime = elapsedRealtime
            lastClickName = className
        }
        return allowClick
    }
}
