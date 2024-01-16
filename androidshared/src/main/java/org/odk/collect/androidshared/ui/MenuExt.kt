package org.odk.collect.androidshared.ui

import android.annotation.SuppressLint
import android.view.Menu
import androidx.appcompat.view.menu.MenuBuilder

/**
 * Currently, there is no public API to add icons to popup menus.
 * The following workaround is for API 21+, and uses library-only APIs,
 * and is not guaranteed to work in future versions.
 */
@SuppressLint("RestrictedApi")
fun Menu.enableIconsVisibility() {
    if (this is MenuBuilder) {
        this.setOptionalIconsVisible(true)
    }
}
