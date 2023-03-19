package org.odk.collect.android.projects

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.MetaKeys

class CurrentProjectProviderTest {

    private val projectsRepository = InMemProjectsRepository()
    private val settingsProvider = InMemSettingsProvider()
    private val metaSettings = settingsProvider.getMetaSettings()
    private val currentProjectProvider =
        CurrentProjectProvider(settingsProvider, projectsRepository)

    @Test
    fun `A project should be returned after calling getCurrentProject() if there is a project for given id`() {
        val project = projectsRepository.save(Project.New("ProjectX", "X", "#00FF00"))
        metaSettings.save(MetaKeys.CURRENT_PROJECT_ID, project.uuid)

        assertThat(currentProjectProvider.getCurrentProject(), `is`(project))
    }

    @Test
    fun `save() on meta settings should be called after current project is set`() {
        currentProjectProvider.setCurrentProject("123e4567")
        assertThat(metaSettings.getString(MetaKeys.CURRENT_PROJECT_ID), equalTo("123e4567"))
    }

    @Test(expected = IllegalStateException::class)
    fun `getCurrentProject throws IllegalStateException when there is no current project`() {
        currentProjectProvider.getCurrentProject()
    }

    @Test
    fun `getCurrentProject returns first project when there is no current project but there are projects`() {
        val firstProject = projectsRepository.save(Project.New("ProjectX", "X", "#00FF00"))
        projectsRepository.save(Project.New("ProjectY", "Y", "#00FF00"))
        assertThat(currentProjectProvider.getCurrentProject(), `is`(firstProject))
    }

    @Test(expected = IllegalStateException::class)
    fun `getCurrentProject throws IllegalStateException when current project does not exist`() {
        currentProjectProvider.setCurrentProject("123e4567")
        currentProjectProvider.getCurrentProject()
    }
}
