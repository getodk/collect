package org.odk.collect.android.projects

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SharedPreferencesSettings
import org.odk.collect.android.projects.ProjectImporter.Companion.DEMO_PROJECT_ID
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class ProjectImporterTest {
    private lateinit var projectsRepository: ProjectsRepository
    private lateinit var metaSettings: SharedPreferencesSettings
    private lateinit var projectImporter: ProjectImporter

    @Before
    fun setup() {
        projectsRepository = Mockito.mock(ProjectsRepository::class.java)
        metaSettings = Mockito.mock(SharedPreferencesSettings::class.java)
        projectImporter = ProjectImporter(projectsRepository, metaSettings)
    }

    @Test
    fun `Default project should be imported when importDemoProject() called`() {
        projectImporter.importDemoProject()
        verify(projectsRepository).add(Project("Demo project", "D", "#3e9fcc", DEMO_PROJECT_ID))
        verify(metaSettings).save(MetaKeys.CURRENT_PROJECT_ID, DEMO_PROJECT_ID)
    }

    @Test
    fun `Existed project should be imported when importExistingProject() called`() {
        projectImporter.importExistingProject()
        verify(projectsRepository).add(Project("Demo project", "D", "#3e9fcc", DEMO_PROJECT_ID))
        verify(metaSettings).save(MetaKeys.CURRENT_PROJECT_ID, DEMO_PROJECT_ID)
    }
}
