package org.odk.collect.androidshared.network

import android.net.NetworkInfo

interface NetworkStateProvider {
    val isDeviceOnline: Boolean
    val networkInfo: NetworkInfo?
}
