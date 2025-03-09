package org.odk.collect.androidshared.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter

interface BroadcastReceiverRegister {
    fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter)

    fun unregisterReceiver(receiver: BroadcastReceiver)
}

class BroadcastReceiverRegisterImpl(private val context: Context) : BroadcastReceiverRegister {
    override fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        context.registerReceiver(receiver, filter)
    }

    override fun unregisterReceiver(receiver: BroadcastReceiver) {
        context.unregisterReceiver(receiver)
    }
}
