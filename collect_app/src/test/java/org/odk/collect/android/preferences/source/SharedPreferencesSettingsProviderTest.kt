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
    fun `The same unprotected settings should always be returned for current project`() {
        assertThat(settingsProvider.getUnprotectedSettings(), `is`(settingsProvider.getUnprotectedSettings()))
    }

    @Test
    fun `The same unprotected settings should always be returned for given projectId`() {
        assertThat(settingsProvider.getUnprotectedSettings("1234"), `is`(settingsProvider.getUnprotectedSettings("1234")))
    }

    @Test
    fun `The same unprotected settings should be returned for given projectId and current project if the those are the same`() {
        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, "1234")
        assertThat(settingsProvider.getUnprotectedSettings("1234"), `is`(settingsProvider.getUnprotectedSettings()))
    }

    @Test
    fun `Different unprotected settings should be returned for different projectIds`() {
        assertThat(settingsProvider.getUnprotectedSettings("1234"), `is`(not(settingsProvider.getUnprotectedSettings("4321"))))
    }

    @Test
    fun `Different unprotected settings should be returned for given projectId and current project if those are not the same`() {
        assertThat(settingsProvider.getUnprotectedSettings("1234"), `is`(not(settingsProvider.getUnprotectedSettings())))
    }

    @Test
    fun `The same protected settings should always be returned for current project`() {
        assertThat(settingsProvider.getProtectedSettings(), `is`(settingsProvider.getProtectedSettings()))
    }

    @Test
    fun `The same protected settings should always be returned for given projectId`() {
        assertThat(settingsProvider.getProtectedSettings("1234"), `is`(settingsProvider.getProtectedSettings("1234")))
    }

    @Test
    fun `The same protected settings should be returned for given projectId and current project if those are the same`() {
        settingsProvider.getMetaSettings().save(CURRENT_PROJECT_ID, "1234")
        assertThat(settingsProvider.getProtectedSettings("1234"), `is`(settingsProvider.getProtectedSettings()))
    }

    @Test
    fun `Different protected settings should be returned for different projectIds`() {
        assertThat(settingsProvider.getProtectedSettings("1234"), `is`(not(settingsProvider.getProtectedSettings("4321"))))
    }

    @Test
    fun `Different protected settings should be returned for given projectId and current project if those are not the same`() {
        assertThat(settingsProvider.getProtectedSettings("1234"), `is`(not(settingsProvider.getProtectedSettings())))
    }
}
