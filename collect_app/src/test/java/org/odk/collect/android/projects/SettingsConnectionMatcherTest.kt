package org.odk.collect.android.projects

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.support.InMemSettingsProvider
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project

class SettingsConnectionMatcherTest {
    private val inMemProjectsRepository = InMemProjectsRepository()
    private val inMemSettingsProvider = InMemSettingsProvider()
    private val settingsConnectionMatcher = SettingsConnectionMatcher(inMemProjectsRepository, inMemSettingsProvider)

    @Before
    fun setUp() {
        inMemProjectsRepository.deleteAll()
        inMemSettingsProvider.clearAll()
    }

    @Test
    fun `returns null when no projects exist`() {
        val jsonSettings = getServerSettingsJson("https://demo.getodk.org")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`(nullValue()))
    }

    @Test
    fun `returns a match when urls match`() {
        createServerProject("a uuid", "https://demo.getodk.org", "")
        val jsonSettings = getServerSettingsJson("https://demo.getodk.org")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("a uuid"))
    }

    @Test
    fun `returns null when urls match and usernames don't match`() {
        createServerProject("a uuid", "https://demo.getodk.org", "")
        val jsonSettings = getServerSettingsJson("https://demo.getodk.org", "foo")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`(nullValue()))
    }

    @Test
    fun `returns a match when urls and usernames match`() {
        createServerProject("a uuid", "https://demo.getodk.org", "foo")
        val jsonSettings = getServerSettingsJson("https://demo.getodk.org", "foo")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("a uuid"))
    }

    @Test
    fun `returns a match when urls and usernames match and there are other settings that don't match`() {
        createServerProject("a uuid", "https://demo.getodk.org", "foo")
        val jsonSettings = "{ \"general\": { \"server_url\": \"https://demo.getodk.org\", \"username\": \"foo\", \"password\": \"bar\" } }"

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("a uuid"))
    }

    @Test
    fun `returns null when protocol is Google Drive and accounts don't match`() {
        createGoogleDriveProject("a uuid", "foo@bar.baz")
        val jsonSettings = getGoogleDriveSettingsJson("baz@bar.quux")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`(nullValue()))
    }

    @Test
    fun `returns a match when protocol is Google Drive and accounts match`() {
        createGoogleDriveProject("a uuid", "foo@bar.baz")
        val jsonSettings = getGoogleDriveSettingsJson("foo@bar.baz")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("a uuid"))
    }

    @Test
    fun `returns uuid of match when there are multiple projects`() {
        createServerProject("a uuid", "https://demo.getodk.org", "foo")
        createGoogleDriveProject("another uuid", "foo@bar.baz")
        val jsonSettings = getGoogleDriveSettingsJson("foo@bar.baz")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("another uuid"))
    }

    @Test
    fun `returns uuid of first match when there are multiple matches`() {
        createServerProject("a uuid", "https://demo.getodk.org", "foo")
        createGoogleDriveProject("another uuid", "foo@bar.baz")
        createServerProject("uuid 3", "https://foo.org", "foo")
        createServerProject("uuid 4", "https://foo.org", "foo")

        val jsonSettings = getServerSettingsJson("https://foo.org", "foo")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("uuid 3"))
    }

    private fun createServerProject(projectId: String, url: String, username: String) {
        inMemProjectsRepository.save(Project.Saved(projectId, "no-op", "n", "#ffffff"))

        val generalSettings = inMemSettingsProvider.getGeneralSettings(projectId)
        generalSettings.save(GeneralKeys.KEY_PROTOCOL, GeneralKeys.PROTOCOL_SERVER)
        generalSettings.save(GeneralKeys.KEY_SERVER_URL, url)
        generalSettings.save(GeneralKeys.KEY_USERNAME, username)
    }

    private fun createGoogleDriveProject(projectId: String, account: String) {
        inMemProjectsRepository.save(Project.Saved(projectId, "no-op", "n", "#ffffff"))

        val generalSettings = inMemSettingsProvider.getGeneralSettings(projectId)
        generalSettings.save(GeneralKeys.KEY_PROTOCOL, GeneralKeys.PROTOCOL_GOOGLE_SHEETS)
        generalSettings.save(GeneralKeys.KEY_SELECTED_GOOGLE_ACCOUNT, account)
    }

    private fun getServerSettingsJson(url: String): String {
        return "{ \"general\": { \"server_url\": \"$url\" } }"
    }

    private fun getServerSettingsJson(url: String, username: String): String {
        return "{ \"general\": { \"server_url\": \"$url\", \"username\": \"$username\" } }"
    }

    private fun getGoogleDriveSettingsJson(account: String): String {
        return "{ \"general\": { \"protocol\": \"google_sheets\", \"selected_google_account\": \"$account\" } }"
    }
}
