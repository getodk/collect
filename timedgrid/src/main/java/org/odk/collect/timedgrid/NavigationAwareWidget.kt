package org.odk.collect.timedgrid

import androidx.annotation.StringRes

interface NavigationAwareWidget {

    /**
     * Returns NavigationBlock if navigation should be blocked, or null if navigation is allowed.
     */
    fun shouldBlockNavigation(): NavigationWarning?
}

data class NavigationWarning(
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int
)
