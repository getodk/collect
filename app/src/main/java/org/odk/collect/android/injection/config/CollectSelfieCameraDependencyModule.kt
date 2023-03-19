package org.odk.collect.android.injection.config

import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.selfiecamera.SelfieCameraDependencyModule

class CollectSelfieCameraDependencyModule(private val permissionsChecker: () -> PermissionsChecker) : SelfieCameraDependencyModule() {
    override fun providesPermissionChecker(): PermissionsChecker {
        return permissionsChecker()
    }
}
