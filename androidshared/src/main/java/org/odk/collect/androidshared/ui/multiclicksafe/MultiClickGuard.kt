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

    // Debounce multiple clicks within the same screen
    @JvmStatic
    @JvmOverloads
    fun allowClick(className: String = javaClass.name, clickDebounceMs: Long = 1000): Boolean {
        if (test) {
            return true
        }
        val elapsedRealtime = SystemClock.elapsedRealtime()
        val isSameClass = className == lastClickName
        val isBeyondThreshold = elapsedRealtime - lastClickTime > clickDebounceMs
        val isBeyondTestThreshold =
            lastClickTime == 0L || lastClickTime == elapsedRealtime // just for tests

        val allowClick = !isSameClass || isBeyondThreshold || isBeyondTestThreshold

        if (allowClick) {
            lastClickTime = elapsedRealtime
            lastClickName = className
        }
        return allowClick
    }

    /*
    Utilizing screen names instead of class names offers a more robust approach, as elements placed
    on the same user interface screen may potentially belong to distinct classes (such as fragments or custom views).
    Consequently, employing screen names for identification ensures that these elements are recognized
    as components of the same screen, disabling rapid clicking.
     */
    enum class ScreenName {
        MAIN_MENU
    }
}
