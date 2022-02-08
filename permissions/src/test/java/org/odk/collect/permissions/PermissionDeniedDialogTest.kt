package org.odk.collect.permissions

import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class PermissionDeniedDialogTest {

    @get:Rule
    val activityRule = RecordedIntentsRule()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    private val args = Bundle().apply {
        putInt(PermissionDeniedDialog.TITLE, R.string.camera_runtime_permission_denied_title)
        putInt(PermissionDeniedDialog.MESSAGE, R.string.camera_runtime_permission_denied_desc)
        putInt(PermissionDeniedDialog.ICON, R.drawable.ic_photo_camera)
    }

    @Test
    fun `The dialog should display proper content`() {
        val scenario = launcherRule.launchDialogFragment(PermissionDeniedDialog::class.java, args)
        scenario.onFragment {
            // Button positive
            assertThat((it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_POSITIVE)).visibility, `is`(View.VISIBLE))
            assertThat((it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_POSITIVE)).text, `is`(ApplicationProvider.getApplicationContext<Application>().getString(R.string.ok)))

            // Button neutral
            assertThat((it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_NEUTRAL)).visibility, `is`(View.VISIBLE))
            assertThat((it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_NEUTRAL)).text, `is`(ApplicationProvider.getApplicationContext<Application>().getString(R.string.open_settings)))

            // Button negative
            assertThat((it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_NEGATIVE)).visibility, `is`(View.GONE))

            // Title
            val titleId: Int = ApplicationProvider.getApplicationContext<Application>().resources.getIdentifier("alertTitle", "id", ApplicationProvider.getApplicationContext<Application>().packageName)
            assertThat((it.dialog!!.findViewById(titleId) as TextView).text, `is`(ApplicationProvider.getApplicationContext<Application>().getString(R.string.camera_runtime_permission_denied_title)))

            // Message
            assertThat((it.dialog!!.findViewById(android.R.id.message) as TextView).text, `is`(ApplicationProvider.getApplicationContext<Application>().getString(R.string.camera_runtime_permission_denied_desc)))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = launcherRule.launchDialogFragment(PermissionDeniedDialog::class.java, args)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on the POSITIVE button`() {
        val scenario = launcherRule.launchDialogFragment(PermissionDeniedDialog::class.java, args)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            (it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_POSITIVE)).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `System settings should be opened after clicking on the NEUTRAL button`() {
        val scenario = launcherRule.launchDialogFragment(PermissionDeniedDialog::class.java, args)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            (it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_NEUTRAL)).performClick()

            Intents.intended(IntentMatchers.hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
            Intents.intended(IntentMatchers.hasData(Uri.fromParts("package", ApplicationProvider.getApplicationContext<Application>().packageName, null)))
        }
    }
}
