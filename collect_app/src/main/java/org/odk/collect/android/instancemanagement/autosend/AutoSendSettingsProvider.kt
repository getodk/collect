package org.odk.collect.android.instancemanagement.autosend

import android.app.Application
import android.net.ConnectivityManager
import org.odk.collect.androidshared.network.NetworkStateProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.enums.AutoSend
import org.odk.collect.settings.enums.SettingsUtils.getAutoSend

class AutoSendSettingsProvider(
    private val application: Application,
    private val networkStateProvider: NetworkStateProvider,
    private val settingsProvider: SettingsProvider
) {

    fun isAutoSendEnabledInSettings(projectId: String? = null): Boolean {
        val currentNetworkInfo = networkStateProvider.networkInfo ?: return false

        val autosend = settingsProvider.getUnprotectedSettings(projectId).getAutoSend(application)
        var sendwifi = autosend == AutoSend.WIFI_ONLY
        var sendnetwork = autosend == AutoSend.CELLULAR_ONLY

        if (autosend == AutoSend.WIFI_AND_CELLULAR) {
            sendwifi = true
            sendnetwork = true
        }

        return currentNetworkInfo.type == ConnectivityManager.TYPE_WIFI &&
            sendwifi || currentNetworkInfo.type == ConnectivityManager.TYPE_MOBILE && sendnetwork
    }
}
