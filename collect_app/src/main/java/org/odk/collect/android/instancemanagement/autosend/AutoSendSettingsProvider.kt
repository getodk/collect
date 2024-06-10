package org.odk.collect.android.instancemanagement.autosend

import android.app.Application
import org.odk.collect.async.Scheduler
import org.odk.collect.async.network.NetworkStateProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.enums.AutoSend
import org.odk.collect.settings.enums.StringIdEnumUtils.getAutoSend

class AutoSendSettingsProvider(
    private val application: Application,
    private val networkStateProvider: NetworkStateProvider,
    private val settingsProvider: SettingsProvider
) {

    fun isAutoSendEnabledInSettings(projectId: String? = null): Boolean {
        val currentNetworkType = networkStateProvider.currentNetwork ?: return false

        val autosend = settingsProvider.getUnprotectedSettings(projectId).getAutoSend(application)
        var sendwifi = autosend == AutoSend.WIFI_ONLY
        var sendnetwork = autosend == AutoSend.CELLULAR_ONLY

        if (autosend == AutoSend.WIFI_AND_CELLULAR) {
            sendwifi = true
            sendnetwork = true
        }

        return currentNetworkType == Scheduler.NetworkType.WIFI &&
            sendwifi || currentNetworkType == Scheduler.NetworkType.CELLULAR && sendnetwork
    }
}
