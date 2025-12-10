package org.odk.collect.androidshared.ui

class AlertStore {

    var enabled = false
    private var recordedAlerts = mutableListOf<String>()

    fun register(alert: String) {
        if (enabled) {
            recordedAlerts.add(alert)
        }
    }

    fun pop(): List<String> {
        val copy = recordedAlerts.toList()
        recordedAlerts.clear()

        return copy
    }
}
