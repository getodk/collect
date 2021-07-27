package org.odk.collect.android.formmanagement

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.mockito.kotlin.mock
import org.odk.collect.android.openrosa.OpenRosaFormSource
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.support.InMemSettingsProvider

class FormSourceProviderTest {

    @Test
    fun `returned source uses project's server when passed`() {
        val settingsProvider = InMemSettingsProvider()
        val settings = settingsProvider.getGeneralSettings("projectId")

        val formSourceProvider = FormSourceProvider(settingsProvider, mock())

        settings.save(ProjectKeys.KEY_SERVER_URL, "http://example.com")
        settings.save(ProjectKeys.KEY_FORMLIST_URL, "/a-path")
        settings.save(ProjectKeys.KEY_USERNAME, "user")
        settings.save(ProjectKeys.KEY_PASSWORD, "pass")
        val formSource = formSourceProvider.get("projectId") as OpenRosaFormSource

        assertThat(formSource.serverURL, `is`("http://example.com"))
        assertThat(formSource.formListPath, `is`("/a-path"))
        assertThat(formSource.webCredentialsUtils.userNameFromPreferences, `is`("user"))
        assertThat(formSource.webCredentialsUtils.passwordFromPreferences, `is`("pass"))
    }
}
