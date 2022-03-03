package org.odk.collect.android.backgroundwork.autosend

import android.net.ConnectivityManager
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class GeneralAutoSendChecker(
    private val networkStateProvider: NetworkStateProvider,
    private val settingsProvider: SettingsProvider
) {

    /**
     * Returns whether the currently-available connection type is included in the app-level auto-send
     * settings.
     *
     * @return true if a connection is available and settings specify it should trigger auto-send,
     * false otherwise.
     */
    fun isAutoSendEnabled(projectId: String): Boolean {
        val networkInfo = networkStateProvider.networkInfo ?: return false

        val autosend = settingsProvider.getUnprotectedSettings(projectId).getString(ProjectKeys.KEY_AUTOSEND)
        var sendwifi = autosend == "wifi_only"
        var sendnetwork = autosend == "cellular_only"

        if (autosend == "wifi_and_cellular") {
            sendwifi = true
            sendnetwork = true
        }
        return networkInfo.type == ConnectivityManager.TYPE_WIFI &&
            sendwifi || networkInfo.type == ConnectivityManager.TYPE_MOBILE && sendnetwork
    }
}
