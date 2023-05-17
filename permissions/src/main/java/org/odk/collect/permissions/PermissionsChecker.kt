package org.odk.collect.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

interface PermissionsChecker {
    fun isPermissionGranted(vararg permissions: String): Boolean
    fun shouldAskForPermission(activity: Activity, vararg permissions: String): Boolean
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

    override fun shouldAskForPermission(activity: Activity, vararg permissions: String): Boolean {
        return permissions.any {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.shouldShowRequestPermissionRationale(it)
            } else {
                true
            }
        }
    }
}
