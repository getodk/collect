package org.odk.collect.android.instancemanagement.autosend

import android.net.ConnectivityManager
import org.odk.collect.androidshared.network.NetworkStateProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class AutoSendSettingsProvider(
    private val networkStateProvider: NetworkStateProvider,
    private val settingsProvider: SettingsProvider
) {

    fun isAutoSendEnabledInSettings(projectId: String? = null): Boolean {
        val currentNetworkInfo = networkStateProvider.networkInfo ?: return false

        val autosend = settingsProvider.getUnprotectedSettings(projectId).getString(ProjectKeys.KEY_AUTOSEND)
        var sendwifi = autosend == "wifi_only"
        var sendnetwork = autosend == "cellular_only"

        if (autosend == "wifi_and_cellular") {
            sendwifi = true
            sendnetwork = true
        }
        return currentNetworkInfo.type == ConnectivityManager.TYPE_WIFI &&
            sendwifi || currentNetworkInfo.type == ConnectivityManager.TYPE_MOBILE && sendnetwork
    }
}
