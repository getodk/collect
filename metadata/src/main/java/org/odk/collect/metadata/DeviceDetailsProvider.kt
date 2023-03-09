package org.odk.collect.metadata

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager

interface DeviceDetailsProvider {
    @get:Throws(SecurityException::class)
    val deviceId: String?

    @get:Throws(SecurityException::class)
    val line1Number: String?
}

class StaticCachingDeviceDetailsProvider(
    private val installIDProvider: InstallIDProvider,
    private val context: Context
) : DeviceDetailsProvider {

    override val deviceId: String
        get() = installIDProvider.installID

    @get:SuppressLint("MissingPermission", "HardwareIds")
    override val line1Number: String?
        get() {
            if (!lineNumberFetched) {
                val telMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                lineNumber = telMgr.line1Number
                lineNumberFetched = true
            }
            return lineNumber
        }

    companion object {
        /**
         * We want to cache the line number statically as fetching it takes several ms and we don't
         * expect it to change during the process lifecycle.
         */
        private var lineNumber: String? = null
        private var lineNumberFetched = false
    }
}
