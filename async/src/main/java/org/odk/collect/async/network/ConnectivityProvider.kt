package org.odk.collect.async.network

import android.content.Context
import android.net.ConnectivityManager
import org.odk.collect.async.Scheduler

class ConnectivityProvider(private val context: Context) : NetworkStateProvider {
    override val currentNetwork: Scheduler.NetworkType?
        get() {
            return if (connectivityManager.activeNetworkInfo?.isConnected == true) {
                when (connectivityManager.activeNetworkInfo?.type) {
                    ConnectivityManager.TYPE_WIFI -> Scheduler.NetworkType.WIFI
                    ConnectivityManager.TYPE_MOBILE -> Scheduler.NetworkType.CELLULAR
                    else -> Scheduler.NetworkType.OTHER
                }
            } else {
                null
            }
        }

    private val connectivityManager: ConnectivityManager
        get() = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}
