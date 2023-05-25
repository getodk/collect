package org.odk.collect.testshared

import android.net.ConnectivityManager
import android.net.NetworkInfo
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.androidshared.network.NetworkStateProvider

class FakeNetworkStateProvider : NetworkStateProvider {
    private var isInternetConnectionEnabled = true

    fun disableInternetConnection() {
        isInternetConnectionEnabled = false
    }

    override val isDeviceOnline: Boolean
        get() = isInternetConnectionEnabled

    override val networkInfo: NetworkInfo
        get() = mock<NetworkInfo>().also {
            whenever(it.type).thenReturn(ConnectivityManager.TYPE_MOBILE)
        }
}
