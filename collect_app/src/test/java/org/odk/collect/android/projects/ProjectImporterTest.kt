package org.odk.collect.android.projects

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SharedPreferencesSettings

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
    fun whenImportDemoProjectCalled_ShouldDefaultProjectBeImported() {
        projectImporter.importDemoProject()
        verify(projectsRepository).add(Project("Demo project", "D", "#3e9fcc", "1"))
        verify(metaSettings).save(MetaKeys.CURRENT_PROJECT_ID, "1")
    }
}
