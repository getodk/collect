package org.odk.collect.testshared

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.odk.collect.androidshared.system.BroadcastReceiverRegister

class FakeBroadcastReceiverRegister : BroadcastReceiverRegister {
    val registeredReceivers = mutableListOf<Pair<String, BroadcastReceiver>>()

    override fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        registeredReceivers.add(Pair(filter.getAction(0), receiver))
    }

    override fun unregisterReceiver(receiver: BroadcastReceiver) {
        registeredReceivers.removeIf { it.second == receiver }
    }

    fun broadcast(context: Context, intent: Intent) {
        registeredReceivers
            .filter { it.first == intent.action }
            .forEach { it.second.onReceive(context, intent) }
    }
}
