package org.odk.collect.android.utilities

import android.content.Context

class AppStateProvider(private val context: Context) {
    fun isFreshInstall(): Boolean {
        return try {
            val firstInstallTime = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
            val lastUpdateTime = context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            firstInstallTime == lastUpdateTime
        } catch (e: Exception) {
            true
        } catch (e: Error) {
            true
        }
    }
}
