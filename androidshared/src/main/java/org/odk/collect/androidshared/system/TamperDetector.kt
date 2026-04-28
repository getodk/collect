package org.odk.collect.androidshared.system

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

object TamperDetector {

    @JvmStatic
    fun isTampered(context: Context, expectedSignature: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && expectedSignature.isNotBlank()) {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
            )
            val signatures = packageInfo.signingInfo?.signingCertificateHistory

            return signatures?.none { sig ->
                val bytes = sig.toByteArray()
                val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
                digest.joinToString(":") { "%02x".format(it).uppercase() } == expectedSignature
            } ?: false
        } else {
            return false
        }
    }
}