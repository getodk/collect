package org.odk.collect.android.preferences.dialogs

import android.content.Context
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.ProjectPreferencesViewModel
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.testshared.RobolectricHelpers
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class ChangeAdminPasswordDialogTest {

    private val settingsProvider = InMemSettingsProvider()
    private val projectPreferencesViewModel = mock<ProjectPreferencesViewModel>()

    @Inject
    lateinit var factory: ProjectPreferencesViewModel.Factory

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesSettingsProvider(context: Context?): SettingsProvider {
                return settingsProvider
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
        val scenario = launcherRule.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.pressBack())
            assertThat(it.dialog, `is`(Matchers.nullValue()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on 'OK'`() {
        val scenario = launcherRule.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(Matchers.nullValue()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on 'CANCEL'`() {
        val scenario = launcherRule.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, Matchers.`is`(true))
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(Matchers.nullValue()))
        }
    }

    @Test
    fun `Setting password and accepting updates the password in settings`() {
        val scenario = launcherRule.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_ADMIN_PW, "")
            it.binding.pwdField.setText("password")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(settingsProvider.getProtectedSettings().getString(ProtectedProjectKeys.KEY_ADMIN_PW), `is`("password"))
        }
    }

    @Test
    fun `Setting password and canceling does not update the password in settings`() {
        val scenario = launcherRule.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_ADMIN_PW, "")
            it.binding.pwdField.setText("password")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(settingsProvider.getProtectedSettings().getString(ProtectedProjectKeys.KEY_ADMIN_PW), `is`(""))
        }
    }

    @Test
    fun `Setting password sets Unlocked state in view model`() {
        val scenario = launcherRule.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.pwdField.setText("password")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            verify(projectPreferencesViewModel).setStateUnlocked()
            verifyNoMoreInteractions(projectPreferencesViewModel)
        }
    }

    @Test
    fun `Removing password sets NotProtected state in view model`() {
        val scenario = launcherRule.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.pwdField.setText("")
            (it.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            verify(projectPreferencesViewModel).setStateNotProtected()
            verifyNoMoreInteractions(projectPreferencesViewModel)
        }
    }

    @Test
    fun `When screen is rotated password and checkbox value is retained`() {
        val scenario = launcherRule.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.pwdField.setText("password")
            it.binding.checkBox2.performClick()
            scenario.recreate()
            assertThat(it.binding.pwdField.text.toString(), `is`("password"))
            assertThat(it.binding.checkBox2.isChecked, `is`(true))
        }
    }

    @Test
    fun `'Show password' displays and hides password`() {
        val scenario = launcherRule.launchDialogFragment(ChangeAdminPasswordDialog::class.java)
        scenario.onFragment {
            it.binding.checkBox2.performClick()
            assertThat(it.binding.pwdField.inputType, `is`(InputType.TYPE_TEXT_VARIATION_PASSWORD))
            it.binding.checkBox2.performClick()
            assertThat(it.binding.pwdField.inputType, `is`(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        }
    }
}
