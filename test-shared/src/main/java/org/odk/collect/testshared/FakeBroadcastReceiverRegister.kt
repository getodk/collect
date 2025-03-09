package org.odk.collect.testshared

import android.content.BroadcastReceiver
import android.content.IntentFilter
import org.odk.collect.androidshared.system.BroadcastReceiverRegister

class FakeBroadcastReceiverRegister : BroadcastReceiverRegister {
    val registeredReceivers = mutableListOf<BroadcastReceiver>()

    override fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        registeredReceivers.add(receiver)
    }

    override fun unregisterReceiver(receiver: BroadcastReceiver) {
        registeredReceivers.remove(receiver)
    }
}
