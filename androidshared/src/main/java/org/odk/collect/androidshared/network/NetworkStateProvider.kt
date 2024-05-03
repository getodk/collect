package org.odk.collect.androidshared.network

import org.odk.collect.async.Scheduler

interface NetworkStateProvider {
    val currentNetwork: Scheduler.NetworkType?

    val isDeviceOnline: Boolean
        get() {
            return currentNetwork != null
        }
}
