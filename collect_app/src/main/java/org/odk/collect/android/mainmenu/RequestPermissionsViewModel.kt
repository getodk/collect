package org.odk.collect.android.mainmenu

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.MetaKeys

class RequestPermissionsViewModel(
    private val settingsProvider: SettingsProvider,
    private val permissionChecker: PermissionsChecker
) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)

    fun shouldAskForPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            false
        } else {
            val permissionsAlreadyRequested =
                settingsProvider.getMetaSettings().getBoolean(MetaKeys.PERMISSIONS_REQUESTED)
            val permissionsGranted =
                permissionChecker.isPermissionGranted(*permissions)

            !(permissionsAlreadyRequested || permissionsGranted)
        }
    }

    fun permissionsRequested() {
        settingsProvider.getMetaSettings().save(MetaKeys.PERMISSIONS_REQUESTED, true)
    }
}
