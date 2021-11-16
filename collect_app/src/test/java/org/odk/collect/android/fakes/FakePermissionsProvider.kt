package org.odk.collect.android.fakes

import android.app.Activity
import androidx.test.platform.app.InstrumentationRegistry
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.permissions.PermissionsProvider

/**
 * Mocked implementation of [PermissionsProvider].
 * The runtime permissions can be stubbed for unit testing
 *
 * @author Shobhit Agarwal
 */
class FakePermissionsProvider :
    PermissionsProvider(PermissionsChecker(InstrumentationRegistry.getInstrumentation().targetContext)) {
    private var isPermissionGranted = false

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

    override fun showAdditionalExplanation(
        activity: Activity,
        title: Int,
        message: Int,
        drawable: Int,
        action: PermissionListener
    ) {
        action.denied()
    }

    fun setPermissionGranted(permissionGranted: Boolean) {
        isPermissionGranted = permissionGranted
    }
}
