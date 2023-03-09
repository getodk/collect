package org.odk.collect.metadata

interface DeviceDetailsProvider {
    @get:Throws(SecurityException::class)
    val deviceId: String?

    @get:Throws(SecurityException::class)
    val line1Number: String?
}
