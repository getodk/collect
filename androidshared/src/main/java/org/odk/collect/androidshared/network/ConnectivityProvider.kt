package org.odk.collect.androidshared.network

import android.content.Context
import android.net.ConnectivityManager
import org.odk.collect.async.Scheduler

class ConnectivityProvider(private val context: Context) : NetworkStateProvider {
    override val isDeviceOnline: Boolean
        get() {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

    override val currentNetwork: Scheduler.NetworkType?
        get() {
            return when (connectivityManager.activeNetworkInfo?.type) {
                ConnectivityManager.TYPE_WIFI -> Scheduler.NetworkType.WIFI
                ConnectivityManager.TYPE_MOBILE -> Scheduler.NetworkType.CELLULAR
                else -> null
            }
        }

    private val connectivityManager: ConnectivityManager
        get() = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}
