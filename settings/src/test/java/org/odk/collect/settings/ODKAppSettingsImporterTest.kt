package org.odk.collect.settings

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.settings.importing.SettingsChangeHandler
import org.odk.collect.settings.support.SettingsUtils.assertSettingsEmpty
import java.lang.RuntimeException

class ODKAppSettingsImporterTest {
    private val projectsRepository = InMemProjectsRepository()
    private val settingsProvider = InMemSettingsProvider()
    private val settingsChangeHandler = mock<SettingsChangeHandler>()

    private val settingsImporter = ODKAppSettingsImporter(
        projectsRepository,
        settingsProvider,
        mapOf("server_url" to "https://demo.getodk.org"),
        emptyMap(),
        listOf("#00000"),
        settingsChangeHandler
    )

    @Test
    fun `accepts valid JSON`() {
        val result = settingsImporter.fromJSON(
            "{\n" +
                "  \"general\": {\n" +
                "  },\n" +
                "  \"admin\": {\n" +
                "  }\n" +
                "}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(true))
    }

    @Test
    fun `rejects JSON without general object`() {
        val result = settingsImporter.fromJSON(
            "{ \"admin\": {}}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(false))
        assertSettingsEmpty(settingsProvider.getUnprotectedSettings())
        assertSettingsEmpty(settingsProvider.getProtectedSettings())
    }

    @Test
    fun `rejects JSON without admin object`() {
        val result = settingsImporter.fromJSON(
            "{ \"general\": {}}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(false))
        assertSettingsEmpty(settingsProvider.getUnprotectedSettings())
        assertSettingsEmpty(settingsProvider.getProtectedSettings())
    }

    @Test
    fun `rejects invalid JSON`() {
        val result = settingsImporter.fromJSON(
            "{\"general\":{*},\"admin\":{}}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(false))
        assertSettingsEmpty(settingsProvider.getUnprotectedSettings())
        assertSettingsEmpty(settingsProvider.getProtectedSettings())
    }

    @Test
    fun `rejects JSON when exception is thrown during importing`() {
        whenever(settingsChangeHandler.onSettingsChanged(any())).thenThrow(RuntimeException::class.java)

        val result = settingsImporter.fromJSON(
            "{\n" +
                "  \"general\": {\n" +
                "  },\n" +
                "  \"admin\": {\n" +
                "  }\n" +
                "}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(false))
    }
}
