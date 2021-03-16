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
class SettingsProviderTest {
    private val settingsProvider = SettingsProvider(ApplicationProvider.getApplicationContext())

    @Before
    fun setup() {
        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, null)
    }

    @Test
    fun `getMetaSettings() should always return the same object no matter what CURRENT_PROJECT_ID is`() {
        val metaSettings = settingsProvider.getMetaSettings()
        assertThat(metaSettings, `is`(settingsProvider.getMetaSettings()))

        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, "1234")
        assertThat(metaSettings, `is`(settingsProvider.getMetaSettings()))
    }

    @Test
    fun `getGeneralSettings() should always return the same object for given projectId`() {
        assertThat(settingsProvider.getGeneralSettings(), `is`(settingsProvider.getGeneralSettings()))
        assertThat(settingsProvider.getGeneralSettings("1234"), `is`(settingsProvider.getGeneralSettings("1234")))
        assertThat(settingsProvider.getGeneralSettings("1234"), `is`(not(settingsProvider.getGeneralSettings())))
    }

    @Test
    fun `getAdminSettings() should always return the same object for given projectId`() {
        assertThat(settingsProvider.getAdminSettings(), `is`(settingsProvider.getAdminSettings()))
        assertThat(settingsProvider.getAdminSettings("1234"), `is`(settingsProvider.getAdminSettings("1234")))
        assertThat(settingsProvider.getAdminSettings("1234"), `is`(not(settingsProvider.getAdminSettings())))
    }

    @Test
    fun `Proper general settings should be used depending on CURRENT_PROJECT_ID`() {
        val generalSettings = settingsProvider.getGeneralSettings()

        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, "1234")

        val generalSettings2 = settingsProvider.getGeneralSettings()
        assertThat(generalSettings, `is`(not(generalSettings2)))

        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, null)

        val generalSettings3 = settingsProvider.getGeneralSettings()
        assertThat(generalSettings, `is`(generalSettings3))

        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, "1234")

        val generalSettings4 = settingsProvider.getGeneralSettings()
        assertThat(generalSettings2, `is`(generalSettings4))
    }

    @Test
    fun `Proper admin settings should be used depending on CURRENT_PROJECT_ID`() {
        val adminSettings = settingsProvider.getAdminSettings()

        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, "1234")

        val adminSettings2 = settingsProvider.getAdminSettings()
        assertThat(adminSettings, `is`(not(adminSettings2)))

        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, null)

        val adminSettings3 = settingsProvider.getAdminSettings()
        assertThat(adminSettings, `is`(adminSettings3))

        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, "1234")

        val adminSettings4 = settingsProvider.getAdminSettings()
        assertThat(adminSettings2, `is`(adminSettings4))
    }
}
