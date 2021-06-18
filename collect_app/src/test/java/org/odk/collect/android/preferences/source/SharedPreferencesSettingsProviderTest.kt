package org.odk.collect.android.preferences.source

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.preferences.keys.MetaKeys.CURRENT_PROJECT_ID

@RunWith(AndroidJUnit4::class)
class SharedPreferencesSettingsProviderTest {

    private val settingsProvider = SharedPreferencesSettingsProvider(ApplicationProvider.getApplicationContext())

    @Before
    fun setup() {
        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, null)
    }

    @Test
    fun `The same mata settings should always be returned no matter what current project is`() {
        val metaSettings = settingsProvider.getMetaSettings()
        assertThat(metaSettings, `is`(settingsProvider.getMetaSettings()))

        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, "1234")
        assertThat(metaSettings, `is`(settingsProvider.getMetaSettings()))
    }

    @Test
    fun `The same general settings should always be returned for current project`() {
        assertThat(settingsProvider.getGeneralSettings(), `is`(settingsProvider.getGeneralSettings()))
    }

    @Test
    fun `The same general settings should always be returned for given projectId`() {
        assertThat(settingsProvider.getGeneralSettings("1234"), `is`(settingsProvider.getGeneralSettings("1234")))
    }

    @Test
    fun `The same general settings should be returned for given projectId and current project if the those are the same`() {
        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, "1234")
        assertThat(settingsProvider.getGeneralSettings("1234"), `is`(settingsProvider.getGeneralSettings()))
    }

    @Test
    fun `Different general settings should be returned for different projectIds`() {
        assertThat(settingsProvider.getGeneralSettings("1234"), `is`(not(settingsProvider.getGeneralSettings("4321"))))
    }

    @Test
    fun `Different general settings should be returned for given projectId and current project if those are not the same`() {
        assertThat(settingsProvider.getGeneralSettings("1234"), `is`(not(settingsProvider.getGeneralSettings())))
    }

    @Test
    fun `The same admin settings should always be returned for current project`() {
        assertThat(settingsProvider.getAdminSettings(), `is`(settingsProvider.getAdminSettings()))
    }

    @Test
    fun `The same admin settings should always be returned for given projectId`() {
        assertThat(settingsProvider.getAdminSettings("1234"), `is`(settingsProvider.getAdminSettings("1234")))
    }

    @Test
    fun `The same admin settings should be returned for given projectId and current project if those are the same`() {
        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, "1234")
        assertThat(settingsProvider.getAdminSettings("1234"), `is`(settingsProvider.getAdminSettings()))
    }

    @Test
    fun `Different admin settings should be returned for different projectIds`() {
        assertThat(settingsProvider.getAdminSettings("1234"), `is`(not(settingsProvider.getAdminSettings("4321"))))
    }

    @Test
    fun `Different admin settings should be returned for given projectId and current project if those are not the same`() {
        assertThat(settingsProvider.getAdminSettings("1234"), `is`(not(settingsProvider.getAdminSettings())))
    }
}
