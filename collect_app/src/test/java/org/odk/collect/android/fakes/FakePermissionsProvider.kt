package org.odk.collect.android.fakes

import android.app.Activity
import androidx.test.platform.app.InstrumentationRegistry
import org.odk.collect.permissions.ContextCompatPermissionChecker
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsProvider

/**
 * Mocked implementation of [PermissionsProvider].
 * The runtime permissions can be stubbed for unit testing
 *
 * @author Shobhit Agarwal
 */
class FakePermissionsProvider :
    PermissionsProvider(ContextCompatPermissionChecker(InstrumentationRegistry.getInstrumentation().targetContext)) {
    private var isPermissionGranted = false

    val requestedPermissions = mutableListOf<String>()

    override fun requestPermissions(
        activity: Activity,
        listener: PermissionListener,
        vararg permissions: String
    ) {
        requestedPermissions.addAll(permissions)

        if (isPermissionGranted) {
            listener.granted()
        } else {
            listener.denied()
        }
    }

    fun setPermissionGranted(permissionGranted: Boolean) {
        isPermissionGranted = permissionGranted
    }
}
