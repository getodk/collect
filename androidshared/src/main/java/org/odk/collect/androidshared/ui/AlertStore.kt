package org.odk.collect.androidshared.ui

/**
 * Component for recording "alerts". This is useful for testing transient UI elements like toasts,
 * flashes or snackbars that are susceptible to flakiness with assertions running after they have
 * disappeared.
 */
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
