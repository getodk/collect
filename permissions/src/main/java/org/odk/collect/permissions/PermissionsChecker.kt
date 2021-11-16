package org.odk.collect.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

open class PermissionsChecker(private val context: Context) {

    open fun isPermissionGranted(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
