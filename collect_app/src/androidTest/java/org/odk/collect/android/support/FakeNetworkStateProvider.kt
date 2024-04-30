package org.odk.collect.android.support

import org.odk.collect.androidshared.network.NetworkStateProvider
import org.odk.collect.async.Scheduler

class FakeNetworkStateProvider : NetworkStateProvider {

    private var online = true
    private var type: Scheduler.NetworkType? = Scheduler.NetworkType.WIFI

    fun goOnline(networkType: Scheduler.NetworkType) {
        online = true
        type = networkType
    }

    fun goOffline() {
        online = false
        type = null
    }

    override val isDeviceOnline: Boolean
        get() = online
    override val currentNetwork: Scheduler.NetworkType?
        get() = type
}
