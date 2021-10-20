package org.odk.collect.geo

import android.Manifest
import android.app.Activity
import org.odk.collect.androidshared.system.PermissionsChecker
import org.odk.collect.androidshared.ui.ToastUtils

internal object GeoActivityUtils {

    @JvmStatic
    fun requireLocationPermissions(activity: Activity) {
        val permissionGranted = PermissionsChecker(activity).isPermissionGranted(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (!permissionGranted) {
            ToastUtils.showLongToast(activity, R.string.not_granted_permission)
            activity.finish()
        }
    }
}
