package org.odk.collect.android.preferences.screens

import androidx.lifecycle.ViewModel
import androidx.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.ProjectPreferencesViewModel
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.shared.settings.Settings

@RunWith(AndroidJUnit4::class)
class UserInterfacePreferencesFragmentTest {
    private lateinit var generalSettings: Settings
    private lateinit var adminSettings: Settings

    private val adminPasswordProvider = mock<AdminPasswordProvider> {
        on { isAdminPasswordSet } doReturn false
    }
    private val projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectPreferencesViewModel(adminPasswordProvider: AdminPasswordProvider): ProjectPreferencesViewModel.Factory {
                return object : ProjectPreferencesViewModel.Factory(adminPasswordProvider) {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return projectPreferencesViewModel as T
                    }
                }
            }
        })

        CollectHelpers.setupDemoProject()
        generalSettings = TestSettingsProvider.getUnprotectedSettings()
        adminSettings = TestSettingsProvider.getProtectedSettings()
    }

    @Test
    fun `Enabled preferences should be visible in Locked mode`() {
        projectPreferencesViewModel.setStateLocked()

        val scenario = launcherRule.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SHOW_SPLASH)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SPLASH_PATH)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be hidden in Locked mode`() {
        adminSettings.save(ProtectedProjectKeys.KEY_APP_THEME, false)
        adminSettings.save(ProtectedProjectKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_NAVIGATION, false)
        adminSettings.save(ProtectedProjectKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateLocked()

        val scenario = launcherRule.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SHOW_SPLASH)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SPLASH_PATH)!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `Enabled preferences should be visible in Unlocked mode`() {
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = launcherRule.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SHOW_SPLASH)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SPLASH_PATH)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be visible in Unlocked mode`() {
        adminSettings.save(ProtectedProjectKeys.KEY_APP_THEME, false)
        adminSettings.save(ProtectedProjectKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_NAVIGATION, false)
        adminSettings.save(ProtectedProjectKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateUnlocked()

        val scenario = launcherRule.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SHOW_SPLASH)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SPLASH_PATH)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Enabled preferences should be visible in NotProtected mode`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = launcherRule.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SHOW_SPLASH)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SPLASH_PATH)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be hidden in NotProtected mode`() {
        adminSettings.save(ProtectedProjectKeys.KEY_APP_THEME, false)
        adminSettings.save(ProtectedProjectKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_NAVIGATION, false)
        adminSettings.save(ProtectedProjectKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateNotProtected()

        val scenario = launcherRule.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_THEME)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FONT_SIZE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_NAVIGATION)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SHOW_SPLASH)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_SPLASH_PATH)!!.isVisible, `is`(false))
        }
    }
}
