package org.odk.collect.experimental.timedgrid

import androidx.fragment.app.DialogFragment

interface NavigationAwareWidget {
    /**
     * Whenever the navigation should be stopped.
     */
    fun shouldBlockNavigation(): Boolean

    /**
     * If navigation should be stopped (see #shouldBlockNavigation) this returns DialogFragment to present to user.
     */
    fun getWarningDialog(): Class<out DialogFragment>
}
