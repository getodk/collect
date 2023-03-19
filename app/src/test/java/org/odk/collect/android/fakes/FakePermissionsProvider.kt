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

    var cameraPermissionRequested = false

    override fun requestPermissions(
        activity: Activity,
        listener: PermissionListener,
        vararg permissions: String
    ) {
        if (isPermissionGranted) {
            listener.granted()
        } else {
            listener.denied()
        }
    }

    override fun requestCameraPermission(activity: Activity, action: PermissionListener) {
        super.requestCameraPermission(activity, action)
        cameraPermissionRequested = true
    }

    fun setPermissionGranted(permissionGranted: Boolean) {
        isPermissionGranted = permissionGranted
    }
}
