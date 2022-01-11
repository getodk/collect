package org.odk.collect.android.preferences.screens

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.odk.collect.android.R
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.ProjectPreferencesViewModel
import org.odk.collect.android.preferences.utilities.FormUpdateMode
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.async.Scheduler
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.shared.settings.Settings
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class FormManagementPreferencesFragmentTest {
    private lateinit var projectID: String
    private lateinit var context: Context
    private lateinit var generalSettings: Settings
    private lateinit var adminSettings: Settings

    private val adminPasswordProvider = mock<AdminPasswordProvider> {
        on { isAdminPasswordSet } doReturn false
    }
    private val instanceSubmitScheduler = mock<InstanceSubmitScheduler>()

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

            override fun providesFormSubmitManager(scheduler: Scheduler, settingsProvider: SettingsProvider, application: Application): InstanceSubmitScheduler {
                return instanceSubmitScheduler
            }
        })

        projectID = CollectHelpers.setupDemoProject()
        context = ApplicationProvider.getApplicationContext()
        generalSettings = TestSettingsProvider.getUnprotectedSettings()
        adminSettings = TestSettingsProvider.getProtectedSettings()
    }

    @Test
    fun `When Google Drive used as server shows update mode as manual and disable prefs`() {
        generalSettings.save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
        generalSettings.save(ProjectKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            assertThat(
                f.findPreference<Preference>(ProjectKeys.KEY_FORM_UPDATE_MODE)!!.summary,
                `is`(context.getString(R.string.manual))
            )
            assertThat(
                generalSettings.getString(ProjectKeys.KEY_FORM_UPDATE_MODE),
                `is`(FormUpdateMode.MATCH_EXACTLY.getValue(context))
            )
            assertThat(
                f.findPreference<Preference>(ProjectKeys.KEY_FORM_UPDATE_MODE)!!.isEnabled,
                `is`(false)
            )
            assertThat(
                f.findPreference<Preference>(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isEnabled,
                `is`(false)
            )
            assertThat(
                f.findPreference<Preference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)!!.isEnabled,
                `is`(false)
            )
        }
    }

    @Test
    fun `When 'Manual Updates' enabled disables prefs`() {
        generalSettings.save(ProjectKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MANUAL.getValue(context))
        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            assertThat(
                f.findPreference<Preference>(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isEnabled,
                `is`(false)
            )
            assertThat(
                f.findPreference<Preference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)!!.isEnabled,
                `is`(false)
            )
        }
    }

    @Test
    fun `When 'Previously Downloaded Only 'enabled disables prefs`() {
        generalSettings.save(ProjectKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY.getValue(context))
        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            assertThat(
                f.findPreference<Preference>(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isEnabled,
                `is`(true)
            )
            assertThat(
                f.findPreference<Preference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)!!.isEnabled,
                `is`(true)
            )
        }
    }

    @Test
    fun `When 'Match Exactly' enabled disables prefs`() {
        generalSettings.save(ProjectKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))
        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            assertThat(
                f.findPreference<Preference>(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isEnabled,
                `is`(true)
            )
            assertThat(
                f.findPreference<Preference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)!!.isEnabled,
                `is`(false)
            )
        }
    }

    @Test
    fun `When 'Match Exactly' enabled and 'Automatic Download' disabled shows 'Automatic Download' as checked`() {
        generalSettings.save(ProjectKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))
        generalSettings.save(ProjectKeys.KEY_AUTOMATIC_UPDATE, false)
        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            val automaticDownload = f.findPreference<CheckBoxPreference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)
            assertThat(automaticDownload!!.isChecked, `is`(true))
            assertThat(
                generalSettings.getBoolean(ProjectKeys.KEY_AUTOMATIC_UPDATE),
                `is`(false)
            )
        }
    }

    @Test
    fun `When 'Manual Updates' enabled and 'Automatic Download' enabled shows 'Automatic Download' as not checked`() {
        generalSettings.save(ProjectKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MANUAL.getValue(context))
        generalSettings.save(ProjectKeys.KEY_AUTOMATIC_UPDATE, true)
        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            val automaticDownload = f.findPreference<CheckBoxPreference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)
            assertThat(automaticDownload!!.isChecked, `is`(false))
            assertThat(
                generalSettings.getBoolean(ProjectKeys.KEY_AUTOMATIC_UPDATE),
                `is`(true)
            )
        }
    }

    @Test
    fun `When Google Drive used as server and 'Automatic Download' enabled shows 'Automatic Download' as not checked`() {
        generalSettings.save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
        generalSettings.save(ProjectKeys.KEY_AUTOMATIC_UPDATE, true)
        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            val automaticDownload = f.findPreference<CheckBoxPreference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)
            assertThat(automaticDownload!!.isChecked, `is`(false))
            assertThat(
                generalSettings.getBoolean(ProjectKeys.KEY_AUTOMATIC_UPDATE),
                `is`(true)
            )
        }
    }

    @Test
    fun `When 'Manual Updates' enabled and 'Automatic Download' disabled setting to 'Previously Downloaded' resets 'Automatic Download'`() {
        generalSettings.save(ProjectKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MANUAL.getValue(context))
        generalSettings.save(ProjectKeys.KEY_AUTOMATIC_UPDATE, false)
        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            val updateMode = f.findPreference<ListPreference>(ProjectKeys.KEY_FORM_UPDATE_MODE)
            updateMode!!.value = FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY.getValue(context)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            val automaticDownload = f.findPreference<CheckBoxPreference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)
            assertThat(automaticDownload!!.isChecked, `is`(false))
            assertThat(
                generalSettings.getBoolean(ProjectKeys.KEY_AUTOMATIC_UPDATE),
                `is`(false)
            )
        }
    }

    @Test
    fun `Changing Form Update Mode should not cause any crash if related preferences are disabled in Protected Settings`() {
        adminSettings.save(ProtectedProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(ProtectedProjectKeys.KEY_AUTOMATIC_UPDATE, false)
        val scenario = launcherRule.launch(
            FormManagementPreferencesFragment::class.java
        )
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            assertThat(f.findPreference<Preference>(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isVisible, `is`(false))
            assertThat(f.findPreference<Preference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)!!.isVisible, `is`(false))
            val updateMode = f.findPreference<ListPreference>(ProjectKeys.KEY_FORM_UPDATE_MODE)
            updateMode!!.value = FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY.getValue(context)
            updateMode.value = FormUpdateMode.MATCH_EXACTLY.getValue(context)
            updateMode.value = FormUpdateMode.MANUAL.getValue(context)
        }
    }

    @Test
    fun `Enabled preferences should be visible in Locked mode`() {
        projectPreferencesViewModel.setStateLocked()

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FORM_UPDATE_MODE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOSEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_DELETE_AFTER_SEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_COMPLETED_DEFAULT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_CONSTRAINT_BEHAVIOR)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIGH_RESOLUTION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_IMAGE_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_GUIDANCE_HINT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_EXTERNAL_APP_RECORDING)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_INSTANCE_SYNC)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be hidden in Locked mode`() {
        adminSettings.save(ProtectedProjectKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(ProtectedProjectKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(ProtectedProjectKeys.KEY_AUTOSEND, false)
        adminSettings.save(ProtectedProjectKeys.KEY_DELETE_AFTER_SEND, false)
        adminSettings.save(ProtectedProjectKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(ProtectedProjectKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(ProtectedProjectKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(ProtectedProjectKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(ProtectedProjectKeys.KEY_EXTERNAL_APP_RECORDING, false)
        adminSettings.save(ProtectedProjectKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateLocked()

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FORM_UPDATE_MODE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOSEND)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_DELETE_AFTER_SEND)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_COMPLETED_DEFAULT)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_CONSTRAINT_BEHAVIOR)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIGH_RESOLUTION)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_IMAGE_SIZE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_GUIDANCE_HINT)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_EXTERNAL_APP_RECORDING)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_INSTANCE_SYNC)!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `Enabled preferences should be visible in Unlocked mode`() {
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FORM_UPDATE_MODE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOSEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_DELETE_AFTER_SEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_COMPLETED_DEFAULT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_CONSTRAINT_BEHAVIOR)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIGH_RESOLUTION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_IMAGE_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_GUIDANCE_HINT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_EXTERNAL_APP_RECORDING)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_INSTANCE_SYNC)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be visible in Unlocked mode`() {
        adminSettings.save(ProtectedProjectKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(ProtectedProjectKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(ProtectedProjectKeys.KEY_AUTOSEND, false)
        adminSettings.save(ProtectedProjectKeys.KEY_DELETE_AFTER_SEND, false)
        adminSettings.save(ProtectedProjectKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(ProtectedProjectKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(ProtectedProjectKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(ProtectedProjectKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(ProtectedProjectKeys.KEY_EXTERNAL_APP_RECORDING, false)
        adminSettings.save(ProtectedProjectKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateUnlocked()

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FORM_UPDATE_MODE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOSEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_DELETE_AFTER_SEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_COMPLETED_DEFAULT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_CONSTRAINT_BEHAVIOR)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIGH_RESOLUTION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_IMAGE_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_GUIDANCE_HINT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_EXTERNAL_APP_RECORDING)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_INSTANCE_SYNC)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Enabled preferences should be visible in NotProtected mode`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FORM_UPDATE_MODE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOSEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_DELETE_AFTER_SEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_COMPLETED_DEFAULT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_CONSTRAINT_BEHAVIOR)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIGH_RESOLUTION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_IMAGE_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_GUIDANCE_HINT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_EXTERNAL_APP_RECORDING)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_INSTANCE_SYNC)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be hidden in NotProtected mode`() {
        adminSettings.save(ProtectedProjectKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(ProtectedProjectKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(ProtectedProjectKeys.KEY_AUTOSEND, false)
        adminSettings.save(ProtectedProjectKeys.KEY_DELETE_AFTER_SEND, false)
        adminSettings.save(ProtectedProjectKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(ProtectedProjectKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(ProtectedProjectKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(ProtectedProjectKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(ProtectedProjectKeys.KEY_EXTERNAL_APP_RECORDING, false)
        adminSettings.save(ProtectedProjectKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateNotProtected()

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_FORM_UPDATE_MODE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOMATIC_UPDATE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_AUTOSEND)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_DELETE_AFTER_SEND)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_COMPLETED_DEFAULT)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_CONSTRAINT_BEHAVIOR)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_HIGH_RESOLUTION)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_IMAGE_SIZE)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_GUIDANCE_HINT)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_EXTERNAL_APP_RECORDING)!!.isVisible, `is`(false))
            assertThat(fragment.findPreference<Preference>(ProjectKeys.KEY_INSTANCE_SYNC)!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `When all preferences in 'Form update' category are hidden, the category should be hidden as well`() {
        adminSettings.save(ProtectedProjectKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(ProtectedProjectKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<PreferenceCategory>("form_update_category")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `When al least one preference in 'Form update' category is visible, the category should be visible as well`() {
        adminSettings.save(ProtectedProjectKeys.KEY_FORM_UPDATE_MODE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, true)
        adminSettings.save(ProtectedProjectKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<PreferenceCategory>("form_update_category")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all preferences in 'Form submission' category are hidden, the category should be hidden as well`() {
        adminSettings.save(ProtectedProjectKeys.KEY_AUTOSEND, false)
        adminSettings.save(ProtectedProjectKeys.KEY_DELETE_AFTER_SEND, false)

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<PreferenceCategory>("form_submission")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `When al least one preference in 'Form submission' category is visible, the category should be visible as well`() {
        adminSettings.save(ProtectedProjectKeys.KEY_AUTOSEND, false)
        adminSettings.save(ProtectedProjectKeys.KEY_DELETE_AFTER_SEND, true)

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<PreferenceCategory>("form_submission")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all preferences in 'Form filling' category are hidden, the category should be hidden as well`() {
        adminSettings.save(ProtectedProjectKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(ProtectedProjectKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(ProtectedProjectKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(ProtectedProjectKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(ProtectedProjectKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(ProtectedProjectKeys.KEY_EXTERNAL_APP_RECORDING, false)

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<PreferenceCategory>("form_filling")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `When al least one preference in 'Form filling' category is visible, the category should be visible as well`() {
        adminSettings.save(ProtectedProjectKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(ProtectedProjectKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(ProtectedProjectKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(ProtectedProjectKeys.KEY_IMAGE_SIZE, true)
        adminSettings.save(ProtectedProjectKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(ProtectedProjectKeys.KEY_EXTERNAL_APP_RECORDING, false)

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<PreferenceCategory>("form_filling")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When all preferences in 'Form import' category are hidden, the category should be hidden as well`() {
        adminSettings.save(ProtectedProjectKeys.KEY_INSTANCE_FORM_SYNC, false)

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<PreferenceCategory>("form_import")!!.isVisible, `is`(false))
        }
    }

    @Test
    fun `When al least one preference in 'Form import' category is visible, the category should be visible as well`() {
        adminSettings.save(ProtectedProjectKeys.KEY_INSTANCE_FORM_SYNC, true)

        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<PreferenceCategory>("form_import")!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `When Auto send preference is enabled, finalized forms should be scheduled for submission`() {
        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            fragment.findPreference<ListPreference>(ProjectKeys.KEY_AUTOSEND)!!.value = "wifi"
        }
        verify(instanceSubmitScheduler).scheduleSubmit(projectID)
    }

    @Test
    fun `When Auto send preference is disabled, no submissions should be scheduled`() {
        val scenario = launcherRule.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            fragment.findPreference<ListPreference>(ProjectKeys.KEY_AUTOSEND)!!.value = "off"
        }
        verify(instanceSubmitScheduler, never()).scheduleSubmit(projectID)
    }
}
