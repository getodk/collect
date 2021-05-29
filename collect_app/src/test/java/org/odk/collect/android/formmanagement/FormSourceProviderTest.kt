package org.odk.collect.android.formmanagement

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.openrosa.OpenRosaFormSource
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.testshared.InMemSettings

class FormSourceProviderTest {

    @Test
    fun `returned source uses project's server when passed`() {
        val settings = InMemSettings()
        val settingsProvider = mock<SettingsProvider> {
            on { getGeneralSettings("projectId") } doReturn settings
        }

        val formSourceProvider = FormSourceProvider(settingsProvider, mock(), mock())

        settings.save(GeneralKeys.KEY_SERVER_URL, "http://example.com")
        settings.save(GeneralKeys.KEY_FORMLIST_URL, "/a-path")
        settings.save(GeneralKeys.KEY_USERNAME, "user")
        settings.save(GeneralKeys.KEY_PASSWORD, "pass")
        val formSource = formSourceProvider.get("projectId") as OpenRosaFormSource

        assertThat(formSource.serverURL, `is`("http://example.com"))
        assertThat(formSource.formListPath, `is`("/a-path"))
        assertThat(formSource.webCredentialsUtils.userNameFromPreferences, `is`("user"))
        assertThat(formSource.webCredentialsUtils.passwordFromPreferences, `is`("pass"))
    }
}
