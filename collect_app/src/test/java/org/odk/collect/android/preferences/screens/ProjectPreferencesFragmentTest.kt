package org.odk.collect.android.preferences.screens

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.ViewModel
import androidx.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.ProjectPreferencesViewModel
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.shared.Settings

@RunWith(AndroidJUnit4::class)
class ProjectPreferencesFragmentTest {
    private lateinit var generalSettings: Settings
    private lateinit var adminSettings: Settings

    private val adminPasswordProvider = mock<AdminPasswordProvider> {
        on { isAdminPasswordSet } doReturn false
    }
    private val projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectPreferencesViewModel(adminPasswordProvider: AdminPasswordProvider): ProjectPreferencesViewModel.Factory {
                return object : ProjectPreferencesViewModel.Factory(adminPasswordProvider) {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return projectPreferencesViewModel as T
                    }
                }
            }
        })

        CollectHelpers.setupDemoProject()
        generalSettings = TestSettingsProvider.getGeneralSettings()
        adminSettings = TestSettingsProvider.getAdminSettings()
    }

    @Test
    fun `List of preferences should be updated after changing settings in protected settings`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("protocol")!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>("project_display")!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>("user_interface")!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>("maps")!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>("form_management")!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>("user_and_device_identity")!!.isVisible, `is`(true))
        }

        adminSettings.save(AdminKeys.KEY_CHANGE_SERVER, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_PROJECT_DISPLAY, false)
        adminSettings.save(AdminKeys.KEY_APP_THEME, false)
        adminSettings.save(AdminKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(AdminKeys.KEY_NAVIGATION, false)
        adminSettings.save(AdminKeys.KEY_SHOW_SPLASH_SCREEN, false)
        adminSettings.save(AdminKeys.KEY_MAPS, false)
        adminSettings.save(AdminKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false)
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, false)
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(AdminKeys.KEY_EXTERNAL_APP_RECORDING, false)
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false)
        adminSettings.save(AdminKeys.KEY_ANALYTICS, false)

        scenario.recreate()
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("protocol")!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>("project_display")!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>("user_interface")!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>("maps")!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>("form_management")!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>("user_and_device_identity")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `If 'Server' option is enabled in protected settings should be visible in Locked mode`() {
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("protocol")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Server' option is disabled in protected settings should be hidden in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_SERVER, false)
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("protocol")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `If 'Server' option is enabled in protected settings should be visible in Unocked mode`() {
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("protocol")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Server' option is disabled in protected settings should be visible in Unocked mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_SERVER, false)
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("protocol")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Server' option is enabled in protected settings should be visible in NotProtected mode`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("protocol")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Server' option is disabled in protected settings should be hidden in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_SERVER, false)
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("protocol")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `If 'Project display' option is enabled in protected settings should be visible in Locked mode`() {
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("project_display")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Project display' option is disabled in protected settings should be hidden in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_PROJECT_DISPLAY, false)
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("project_display")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `If 'Project display' option is enabled in protected settings should be visible in Unocked mode`() {
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("project_display")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Project display' option is disabled in protected settings should be visible in Unocked mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_PROJECT_DISPLAY, false)
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("project_display")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Project display' option is enabled in protected settings should be visible in NotProtected mode`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("project_display")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Project display' option is disabled in protected settings should be hidden in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_PROJECT_DISPLAY, false)
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("project_display")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `When at least one user interface preference is enabled should preference be visible in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_APP_THEME, false)
        adminSettings.save(AdminKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(AdminKeys.KEY_NAVIGATION, true)
        adminSettings.save(AdminKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_interface")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all user interface preferences are disabled should preference be hidden in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_APP_THEME, false)
        adminSettings.save(AdminKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(AdminKeys.KEY_NAVIGATION, false)
        adminSettings.save(AdminKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_interface")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `When at least one user interface preference is enabled should preference be visible in Unlocked mode`() {
        adminSettings.save(AdminKeys.KEY_APP_THEME, false)
        adminSettings.save(AdminKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(AdminKeys.KEY_NAVIGATION, true)
        adminSettings.save(AdminKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_interface")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all user interface preferences are disabled should preference be visible in Unlocked mode`() {
        adminSettings.save(AdminKeys.KEY_APP_THEME, false)
        adminSettings.save(AdminKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(AdminKeys.KEY_NAVIGATION, false)
        adminSettings.save(AdminKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_interface")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When at least one user interface preference is enabled should preference be visible in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_APP_THEME, false)
        adminSettings.save(AdminKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(AdminKeys.KEY_NAVIGATION, true)
        adminSettings.save(AdminKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_interface")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all user interface preferences are disabled should preference be hidden in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_APP_THEME, false)
        adminSettings.save(AdminKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(AdminKeys.KEY_NAVIGATION, false)
        adminSettings.save(AdminKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_interface")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `If 'Maps' option is enabled in protected settings should be visible in Locked mode`() {
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("maps")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Maps' option is disabled in protected settings should be hidden in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_MAPS, false)
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("maps")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `If 'Maps' option is enabled in protected settings should be visible in Unocked mode`() {
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("maps")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Maps' option is disabled in protected settings should be visible in Unocked mode`() {
        adminSettings.save(AdminKeys.KEY_MAPS, false)
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("maps")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Maps' option is enabled in protected settings should be visible in NotProtected mode`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("maps")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Maps' option is disabled in protected settings should be hidden in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_MAPS, false)
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("maps")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `When at least one Form management preference is enabled should preference be visible in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false)
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, true)
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(AdminKeys.KEY_EXTERNAL_APP_RECORDING, false)
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("form_management")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all Form management preferences are disabled should preference be hidden in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false)
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, false)
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(AdminKeys.KEY_EXTERNAL_APP_RECORDING, false)
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("form_management")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `When at least one Form management preference is enabled should preference be visible in Unlocked mode`() {
        adminSettings.save(AdminKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false)
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, true)
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(AdminKeys.KEY_EXTERNAL_APP_RECORDING, false)
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("form_management")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all Form management preferences are disabled should preference be visible in Unlocked mode`() {
        adminSettings.save(AdminKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false)
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, false)
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(AdminKeys.KEY_EXTERNAL_APP_RECORDING, false)
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("form_management")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When at least one Form management preference is enabled should preference be visible in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false)
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, true)
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(AdminKeys.KEY_EXTERNAL_APP_RECORDING, false)
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("form_management")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all Form management preferences are disabled should preference be hidden in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false)
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, false)
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(AdminKeys.KEY_EXTERNAL_APP_RECORDING, false)
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("form_management")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `When at least one Identity preference is enabled should preference be visible in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false)
        adminSettings.save(AdminKeys.KEY_ANALYTICS, true)

        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_and_device_identity")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all Identity preferences are disabled should preference be hidden in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false)
        adminSettings.save(AdminKeys.KEY_ANALYTICS, false)

        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_and_device_identity")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `When at least one Identity preference is enabled should preference be visible in Unlocked mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false)
        adminSettings.save(AdminKeys.KEY_ANALYTICS, true)

        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_and_device_identity")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all Identity preferences are disabled should preference be visible in Unlocked mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false)
        adminSettings.save(AdminKeys.KEY_ANALYTICS, false)

        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_and_device_identity")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When at least one Identity preference is enabled should preference be visible in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false)
        adminSettings.save(AdminKeys.KEY_ANALYTICS, true)

        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_and_device_identity")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all Identity preferences are disabled should preference be hidden in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false)
        adminSettings.save(AdminKeys.KEY_ANALYTICS, false)

        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("user_and_device_identity")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `If in Locked state protected preferences should be hidden`() {
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("unlock_protected_settings")!!.isVisible, `is`(true))

            assertThat(fragment.findPreference<Preference>(AdminKeys.KEY_CHANGE_ADMIN_PASSWORD)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>("project_management")!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>("access_control")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `If in Unlocked state protected preferences should be visible`() {
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("unlock_protected_settings")!!.isVisible, `is`(false))

            assertThat(fragment.findPreference<Preference>(AdminKeys.KEY_CHANGE_ADMIN_PASSWORD)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>("project_management")!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>("access_control")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If in NotProtected state protected preferences should be visible`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(ProjectPreferencesFragment::class.java)
        scenario.onFragment { fragment: ProjectPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>("unlock_protected_settings")!!.isVisible, `is`(false))

            assertThat(fragment.findPreference<Preference>(AdminKeys.KEY_CHANGE_ADMIN_PASSWORD)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>("project_management")!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>("access_control")!!.isVisible, `is`(true))
        }
    }
}
