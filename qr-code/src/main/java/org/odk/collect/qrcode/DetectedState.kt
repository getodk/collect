package org.odk.collect.qrcode

sealed class DetectedState {
    object None : DetectedState()
    object Potential : DetectedState()
    data class Full(val contents: String) : DetectedState()
}
