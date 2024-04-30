package org.odk.collect.androidshared.network

import org.odk.collect.async.Scheduler

interface NetworkStateProvider {
    val isDeviceOnline: Boolean
    val currentNetwork: Scheduler.NetworkType?
}
