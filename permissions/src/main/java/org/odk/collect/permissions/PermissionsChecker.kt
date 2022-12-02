package org.odk.collect.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

interface PermissionsChecker {
    fun isPermissionGranted(vararg permissions: String): Boolean
}

open class ContextCompatPermissionChecker(private val context: Context) : PermissionsChecker {

    override fun isPermissionGranted(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
