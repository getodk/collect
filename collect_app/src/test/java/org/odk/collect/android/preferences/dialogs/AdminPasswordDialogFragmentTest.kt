package org.odk.collect.android.preferences.dialogs

import android.text.InputType
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.ProjectPreferencesViewModel
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.testshared.RobolectricHelpers
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class AdminPasswordDialogFragmentTest {

    private val adminPasswordProvider = mock<AdminPasswordProvider>()
    private val projectPreferencesViewModel = mock<ProjectPreferencesViewModel>()

    @Inject
    lateinit var factory: ProjectPreferencesViewModel.Factory

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesAdminPasswordProvider(settingsProvider: SettingsProvider): AdminPasswordProvider {
                return adminPasswordProvider
            }

            override fun providesProjectPreferencesViewModel(adminPasswordProvider: AdminPasswordProvider): ProjectPreferencesViewModel.Factory {
                return object : ProjectPreferencesViewModel.Factory(adminPasswordProvider) {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return projectPreferencesViewModel as T
                    }
                }
            }
        })
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = launcherRule.launchDialogFragment(AdminPasswordDialogFragment::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on 'OK'`() {
        val scenario = launcherRule.launchDialogFragment(AdminPasswordDialogFragment::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on 'CANCEL'`() {
        val scenario = launcherRule.launchDialogFragment(AdminPasswordDialogFragment::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `Entering correct password sets Unlocked state in view model`() {
        whenever(adminPasswordProvider.isAdminPasswordSet).thenReturn(true)
        whenever(adminPasswordProvider.adminPassword).thenReturn("password")

        val scenario = launcherRule.launchDialogFragment(AdminPasswordDialogFragment::class.java)
        scenario.onFragment {
            it.binding.editText.setText("password")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            verify(projectPreferencesViewModel).setStateUnlocked()
            verifyNoMoreInteractions(projectPreferencesViewModel)
        }
    }

    @Test
    fun `Entering incorrect password does not set any state in view model`() {
        whenever(adminPasswordProvider.isAdminPasswordSet).thenReturn(true)
        whenever(adminPasswordProvider.adminPassword).thenReturn("password")

        val scenario = launcherRule.launchDialogFragment(AdminPasswordDialogFragment::class.java)
        scenario.onFragment {
            it.binding.editText.setText("incorrect_password")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            verifyNoMoreInteractions(projectPreferencesViewModel)
        }
    }

    @Test
    fun `When screen is rotated password and checkbox value is retained`() {
        val scenario = launcherRule.launchDialogFragment(AdminPasswordDialogFragment::class.java)
        scenario.onFragment {
            it.binding.editText.setText("password")
            it.binding.checkBox.performClick()
            scenario.recreate()
            assertThat(it.binding.editText.text.toString(), `is`("password"))
            assertThat(it.binding.checkBox.isChecked, `is`(true))
        }
    }

    @Test
    fun `'Show password' displays and hides password`() {
        val scenario = launcherRule.launchDialogFragment(AdminPasswordDialogFragment::class.java)
        scenario.onFragment {
            it.binding.checkBox.performClick()
            assertThat(it.binding.editText.inputType, `is`(InputType.TYPE_TEXT_VARIATION_PASSWORD))
            it.binding.checkBox.performClick()
            assertThat(it.binding.editText.inputType, `is`(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        }
    }
}
