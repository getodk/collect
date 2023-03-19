package org.odk.collect.androidshared.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectivityProvider(private val context: Context) : NetworkStateProvider {
    override val isDeviceOnline: Boolean
        get() {
            val networkInfo = networkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    override val networkInfo: NetworkInfo?
        get() = connectivityManager.activeNetworkInfo

    private val connectivityManager: ConnectivityManager
        get() = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}
