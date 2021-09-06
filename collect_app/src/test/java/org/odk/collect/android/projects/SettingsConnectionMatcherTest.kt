package org.odk.collect.android.projects

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.support.InMemSettingsProvider
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project

@RunWith(AndroidJUnit4::class)
class SettingsConnectionMatcherTest {
    private val inMemProjectsRepository = InMemProjectsRepository()
    private val inMemSettingsProvider = InMemSettingsProvider()
    private val settingsConnectionMatcher = SettingsConnectionMatcher(inMemProjectsRepository, inMemSettingsProvider)

    @Test
    fun `returns null when no projects exist`() {
        val jsonSettings = getServerSettingsJson("https://example.com")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`(nullValue()))
    }

    @Test
    fun `returns a matching project uuid when urls match`() {
        createServerProject("a uuid", "https://example.com", "")
        val jsonSettings = getServerSettingsJson("https://example.com")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("a uuid"))
    }

    @Test
    fun `returns a matching project uuid when a default project exists and a user tries to add another default project`() {
        assertThat("Test assumes wrong default", ProjectKeys.defaults[ProjectKeys.KEY_PROTOCOL], `is`(ProjectKeys.PROTOCOL_SERVER))

        val defaultUrl = ProjectKeys.defaults[ProjectKeys.KEY_SERVER_URL] as String
        createServerProject("a uuid", defaultUrl, "")
        val jsonSettings = getDefaultServerSettingsJson()

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("a uuid"))
    }

    @Test
    fun `returns null when urls match and usernames don't match`() {
        createServerProject("a uuid", "https://example.com", "")
        val jsonSettings = getServerSettingsJson("https://example.com", "foo")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`(nullValue()))
    }

    @Test
    fun `returns a matching project uuid when urls and usernames match`() {
        createServerProject("a uuid", "https://example.com", "foo")
        val jsonSettings = getServerSettingsJson("https://example.com", "foo")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("a uuid"))
    }

    @Test
    fun `returns a matching project uuid when urls and usernames match and there are other settings that don't match`() {
        createServerProject("a uuid", "https://example.com", "foo")
        val jsonSettings = "{ \"general\": { \"server_url\": \"https://example.com\", \"username\": \"foo\", \"password\": \"bar\" } }"

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("a uuid"))
    }

    @Test
    fun `returns null when protocol is Google Drive and accounts don't match`() {
        createGoogleDriveProject("a uuid", "foo@bar.baz")
        val jsonSettings = getGoogleDriveSettingsJson("baz@bar.quux")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`(nullValue()))
    }

    @Test
    fun `returns a matching project uuid when protocol is Google Drive and accounts match`() {
        createGoogleDriveProject("a uuid", "foo@bar.baz")
        val jsonSettings = getGoogleDriveSettingsJson("foo@bar.baz")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("a uuid"))
    }

    @Test
    fun `returns a matching project uuid when there are multiple projects`() {
        createServerProject("a uuid", "https://example.com", "foo")
        createGoogleDriveProject("another uuid", "foo@bar.baz")
        val jsonSettings = getGoogleDriveSettingsJson("foo@bar.baz")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("another uuid"))
    }

    @Test
    fun `returns uuid of first matching project when there are multiple matching projects`() {
        createServerProject("a uuid", "https://example.com", "foo")
        createGoogleDriveProject("another uuid", "foo@bar.baz")
        createServerProject("uuid 3", "https://foo.org", "foo")
        createServerProject("uuid 4", "https://foo.org", "foo")

        val jsonSettings = getServerSettingsJson("https://foo.org", "foo")

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`("uuid 3"))
    }

    @Test
    fun `returns null when a project with Google Drive exists and a user tries to add a project with the default settings`() {
        createGoogleDriveProject("a uuid", "foo@bar.baz")
        val jsonSettings = getDefaultServerSettingsJson()

        assertThat(settingsConnectionMatcher.getProjectWithMatchingConnection(jsonSettings), `is`(nullValue()))
    }

    private fun createServerProject(projectId: String, url: String, username: String) {
        inMemProjectsRepository.save(Project.Saved(projectId, "no-op", "n", "#ffffff"))

        val generalSettings = inMemSettingsProvider.getGeneralSettings(projectId)
        generalSettings.save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_SERVER)
        generalSettings.save(ProjectKeys.KEY_SERVER_URL, url)
        generalSettings.save(ProjectKeys.KEY_USERNAME, username)
    }

    private fun createGoogleDriveProject(projectId: String, account: String) {
        inMemProjectsRepository.save(Project.Saved(projectId, "no-op", "n", "#ffffff"))

        val generalSettings = inMemSettingsProvider.getGeneralSettings(projectId)
        generalSettings.save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
        generalSettings.save(ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT, account)
        generalSettings.save(ProjectKeys.KEY_SERVER_URL, "")
        generalSettings.save(ProjectKeys.KEY_USERNAME, "")
    }

    private fun getDefaultServerSettingsJson(): String {
        return "{\"general\":{},\"admin\":{},\"project\":{\"name\":\"Demo project\",\"icon\":\"D\",\"color\":\"#3e9fcc\"}}"
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
