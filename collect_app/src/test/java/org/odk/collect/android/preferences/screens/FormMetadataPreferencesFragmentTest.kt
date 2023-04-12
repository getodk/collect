package org.odk.collect.android.preferences.screens

import android.content.Context
import androidx.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.metadata.InstallIDProvider
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

@RunWith(AndroidJUnit4::class)
class FormMetadataPreferencesFragmentTest {
    private val installIDProvider = mock<InstallIDProvider>()
    private val settingsProvider = InMemSettingsProvider()

    @get:Rule
    var launcherRule = FragmentScenarioLauncherRule()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesInstallIDProvider(settingsProvider: SettingsProvider): InstallIDProvider {
                return installIDProvider
            }

            override fun providesSettingsProvider(context: Context): SettingsProvider {
                return settingsProvider
            }
        })
    }

    @Test
    fun whenMetadataEmpty_preferenceSummariesAreNotSet() {
        whenever(installIDProvider.installID).thenReturn("")

        launcherRule
            .launch(FormMetadataPreferencesFragment::class.java)
            .onFragment {
                assertThat(
                    it.findPreference<Preference>("metadata_username")!!.summary,
                    equalTo("Not set")
                )
                assertThat(
                    it.findPreference<Preference>("metadata_phonenumber")!!.summary,
                    equalTo("Not set")
                )
                assertThat(
                    it.findPreference<Preference>("metadata_email")!!.summary,
                    equalTo("Not set")
                )
                assertThat(
                    it.findPreference<Preference>("deviceid")!!.summary,
                    equalTo(it.context!!.getString(R.string.preference_not_available))
                )
            }
    }

    @Test
    fun whenMetadataNotEmpty_preferenceSummariesAreSet() {
        whenever(installIDProvider.installID).thenReturn("123456789")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_USERNAME, "John")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_PHONENUMBER, "789")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_EMAIL, "john@gmail.com")

        launcherRule
            .launch(FormMetadataPreferencesFragment::class.java)
            .onFragment {
                assertThat(
                    it.findPreference<Preference>("metadata_username")!!.summary,
                    equalTo("John")
                )
                assertThat(
                    it.findPreference<Preference>("metadata_phonenumber")!!.summary,
                    equalTo("789")
                )
                assertThat(
                    it.findPreference<Preference>("metadata_email")!!.summary,
                    equalTo("john@gmail.com")
                )
                assertThat(
                    it.findPreference<Preference>("deviceid")!!.summary,
                    equalTo("123456789")
                )
            }
    }
}
