package org.odk.collect.settings

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.settings.support.SettingsUtils.assertSettingsEmpty

class ODKAppSettingsImporterTest {

    private val projectsRepository = InMemProjectsRepository()
    private val settingsProvider = InMemSettingsProvider()

    private val settingsImporter = ODKAppSettingsImporter(
        projectsRepository,
        settingsProvider,
        emptyMap(),
        emptyMap(),
        emptyList()
    ) { _, _, _ -> }

    @Test
    fun `rejects JSON without general object`() {
        val result = settingsImporter.fromJSON(
            "{ \"admin\": {}}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(false))
        assertSettingsEmpty(settingsProvider.getUnprotectedSettings())
        assertSettingsEmpty(settingsProvider.getUnprotectedSettings())
    }

    @Test
    fun `rejects JSON without admin object`() {
        val result = settingsImporter.fromJSON(
            "{ \"general\": {}}",
            projectsRepository.save(Project.New("Flat", "AS", "#ff0000"))
        )
        assertThat(result, equalTo(false))
        assertSettingsEmpty(settingsProvider.getUnprotectedSettings())
        assertSettingsEmpty(settingsProvider.getUnprotectedSettings())
    }
}
