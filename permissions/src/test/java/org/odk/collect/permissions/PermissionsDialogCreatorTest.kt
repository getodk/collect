package org.odk.collect.permissions

import android.app.Activity
import android.content.DialogInterface
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.testshared.RobolectricHelpers
import org.odk.collect.testshared.RobolectricHelpers.createThemedActivity
import org.robolectric.shadows.ShadowDialog

@RunWith(AndroidJUnit4::class)
class PermissionsDialogCreatorTest {
    private lateinit var activity: Activity
    private val permissionListener = mock<PermissionListener>()

    @get:Rule
    val recordedIntentsRule = RecordedIntentsRule()

    @Before
    fun setup() {
        activity = createThemedActivity(FragmentActivity::class.java, R.style.Theme_MaterialComponents)
    }

    @Test
    fun `PermissionListener should not be called immediatelly after displaying enable gps dialog`() {
        PermissionsDialogCreatorImpl.showEnableGPSDialog(
            activity,
            permissionListener
        )

        verifyNoInteractions(permissionListener)
    }

    @Test
    fun `Settings should be open after clicking on the positive button in enable gps dialog`() {
        PermissionsDialogCreatorImpl.showEnableGPSDialog(
            activity,
            permissionListener
        )

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
        RobolectricHelpers.runLooper()

        Intents.intended(IntentMatchers.hasAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    @Test
    fun `PermissionListener#denied should be called after clicking on the negative button in enable gps dialog`() {
        PermissionsDialogCreatorImpl.showEnableGPSDialog(
            activity,
            permissionListener
        )

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()
        RobolectricHelpers.runLooper()

        verify(permissionListener).denied()
        verifyNoMoreInteractions(permissionListener)
    }

    @Test
    fun `PermissionListener should not be called immediatelly after displaying explanation dialog`() {
        PermissionsDialogCreatorImpl.showAdditionalExplanation(
            activity,
            R.string.camera_runtime_permission_denied_title,
            R.string.camera_runtime_permission_denied_desc,
            R.drawable.ic_photo_camera,
            permissionListener
        )

        verifyNoInteractions(permissionListener)
    }

    @Test
    fun `PermissionListener#additionalExplanationClosed should be called after clicking on the positive button in explanation dialog`() {
        PermissionsDialogCreatorImpl.showAdditionalExplanation(
            activity,
            R.string.camera_runtime_permission_denied_title,
            R.string.camera_runtime_permission_denied_desc,
            R.drawable.ic_photo_camera,
            permissionListener
        )

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
        RobolectricHelpers.runLooper()

        verify(permissionListener).additionalExplanationClosed()
        verifyNoMoreInteractions(permissionListener)
    }

    @Test
    fun `Settings should be open after clicking on the neutral button in explanation dialog`() {
        PermissionsDialogCreatorImpl.showAdditionalExplanation(
            activity,
            R.string.camera_runtime_permission_denied_title,
            R.string.camera_runtime_permission_denied_desc,
            R.drawable.ic_photo_camera,
            permissionListener
        )

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).performClick()
        RobolectricHelpers.runLooper()

        Intents.intended(IntentMatchers.hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
        Intents.intended(IntentMatchers.hasData(Uri.fromParts("package", activity.packageName, null)))

        verifyNoInteractions(permissionListener)
    }
}
