package org.odk.collect.android.projects

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.fragments.support.DialogFragmentHelpers
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.TestScreenContextActivity
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class AutomaticProjectCreatorDialogTest {

    lateinit var activity: TestScreenContextActivity
    lateinit var dialog: AutomaticProjectCreatorDialog

    @Before
    fun setup() {
        activity = CollectHelpers.createThemedActivity(TestScreenContextActivity::class.java)
        dialog = AutomaticProjectCreatorDialog()
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        DialogUtils.showIfNotShowing(AutomaticProjectCreatorDialog::class.java, activity.supportFragmentManager)
        RobolectricHelpers.runLooper()
        DialogFragmentHelpers.assertDialogIsCancellable(true)
    }

    @Test
    fun `The dialog should be dismissed after clicking on the 'Cancel' button`() {
        dialog.show(activity.supportFragmentManager, "TAG")
        RobolectricHelpers.runLooper()
        assertThat(dialog.isVisible, `is`(true))
        dialog.binding.cancelButton.performClick()
        RobolectricHelpers.runLooper()
        assertThat(dialog.isVisible, `is`(false))
    }

    @Test
    fun `The ManualProjectCreatorDialog should be displayed after switching to the manual mode`() {
        dialog.show(activity.supportFragmentManager, "TAG")
        RobolectricHelpers.runLooper()
        assertThat(dialog.isVisible, `is`(true))
        dialog.binding.configureManuallyButton.performClick()
        RobolectricHelpers.runLooper()
        assertThat(activity.supportFragmentManager.findFragmentByTag(ManualProjectCreatorDialog::class.java.name), `is`(notNullValue()))
    }
}
