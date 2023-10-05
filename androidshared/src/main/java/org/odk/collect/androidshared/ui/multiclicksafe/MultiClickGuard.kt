package org.odk.collect.androidshared.ui.multiclicksafe

import android.os.SystemClock

object MultiClickGuard {
    @JvmField
    var test = false

    private var lastClickTime: Long = 0
    private var lastClickName: String = javaClass.name

    @JvmStatic
    fun allowClickFast(className: String = javaClass.name): Boolean {
        return allowClick(className, 500)
    }

    /**
     * Debounce multiple clicks within the same screen
     *
     * @param screenName The name of the screen. If not provided, the Java class name of the element
     * is used. However, this approach is imperfect, as elements on the same screen might belong to
     * different classes. Consequently, clicks on these elements are treated as interactions occurring
     * on two distinct screens, not protecting from rapid clicking.
     */
    @JvmStatic
    @JvmOverloads
    fun allowClick(screenName: String = javaClass.name, clickDebounceMs: Long = 1000): Boolean {
        if (test) {
            return true
        }
        val elapsedRealtime = SystemClock.elapsedRealtime()
        val isSameClass = screenName == lastClickName
        val isBeyondThreshold = elapsedRealtime - lastClickTime > clickDebounceMs
        val isBeyondTestThreshold =
            lastClickTime == 0L || lastClickTime == elapsedRealtime // just for tests

        val allowClick = !isSameClass || isBeyondThreshold || isBeyondTestThreshold

        if (allowClick) {
            lastClickTime = elapsedRealtime
            lastClickName = screenName
        }
        return allowClick
    }
}
