package org.odk.collect.android.utilities

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.StringDescription
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.manifest.AndroidManifest
import org.robolectric.res.Fs

/**
 * Test for checking permissions in [AndroidManifest]
 */
@RunWith(AndroidJUnit4::class)
class PermissionsTest {
    @Test
    fun permissionCheck() {
        val androidManifest = AndroidManifest(
            Fs.fileFromPath("build/intermediates/merged_manifests/debug/AndroidManifest.xml"),
            null,
            null
        )
        val permissions = androidManifest.usedPermissions

        // List of expected permissions to be present in AndroidManifest.xml
        val expectedPermissions = arrayOf(
            "android.permission.READ_PHONE_STATE",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.INTERNET",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.GET_ACCOUNTS",
            "android.permission.USE_CREDENTIALS",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.MANAGE_ACCOUNTS",
            "android.permission.WAKE_LOCK"
        )

        // Checking expected permissions one by one
        for (permission in expectedPermissions) {
            if (!permissions.contains(permission)) {
                showError(permission)
            }
        }
    }

    /**
     * Method to display missing permission error.
     */
    private fun showError(permission: String) {
        val description: Description = StringDescription()
        description.appendText("Expected permission ")
            .appendText(permission)
            .appendText(" is missing from AndroidManifest.xml")

        throw AssertionError(description.toString())
    }
}
