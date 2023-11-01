package org.odk.collect.android.preferences.screens

import androidx.preference.CheckBoxPreference
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.androidshared.utils.getPreference
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.shared.settings.Settings

@RunWith(AndroidJUnit4::class)
class FormEntryAccessPreferencesFragmentTest {

    private lateinit var adminSettings: Settings

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Before
    fun setup() {
        CollectHelpers.setupDemoProject()
        adminSettings = TestSettingsProvider.getProtectedSettings()
    }

    @Test
    fun `when the 'Save as draft' option is unchecked, the 'Finalize' option can't be changed`() {
        adminSettings.save(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, false)
        adminSettings.save(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY, true)

        val scenario = launcherRule.launch(FormEntryAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormEntryAccessPreferencesFragment ->
            assertThat(
                fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).isEnabled,
                equalTo(true)
            )

            assertThat(
                fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY).isEnabled,
                equalTo(false)
            )
        }
    }

    @Test
    fun `when the 'Finalize' option is unchecked, the 'Save as draft' option can't be changed`() {
        adminSettings.save(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, true)
        adminSettings.save(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY, false)

        val scenario = launcherRule.launch(FormEntryAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormEntryAccessPreferencesFragment ->
            assertThat(
                fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).isEnabled,
                equalTo(false)
            )

            assertThat(
                fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY).isEnabled,
                equalTo(true)
            )
        }
    }

    @Test
    fun `when the user unchecks the 'Save as draft' option, the 'Finalize' option becomes disabled`() {
        val scenario = launcherRule.launch(FormEntryAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormEntryAccessPreferencesFragment ->
            assertThat(
                fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY).isEnabled,
                equalTo(true)
            )

            fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).performClick()

            assertThat(
                fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY).isEnabled,
                equalTo(false)
            )
            assertThat(
                fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY).isChecked,
                equalTo(true)
            )
        }
    }

    @Test
    fun `when the user unchecks the 'Finalize' option, the 'Save as draft' option becomes disabled`() {
        val scenario = launcherRule.launch(FormEntryAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormEntryAccessPreferencesFragment ->
            assertThat(
                fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).isEnabled,
                equalTo(true)
            )

            fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_FINALIZE_IN_FORM_ENTRY).performClick()

            assertThat(
                fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).isEnabled,
                equalTo(false)
            )
            assertThat(
                fragment.getPreference<CheckBoxPreference>(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT).isChecked,
                equalTo(true)
            )
        }
    }
}
