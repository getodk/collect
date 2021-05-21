package org.odk.collect.android.projects

import org.odk.collect.android.storage.StorageInitializer
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class ProjectImporter(
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val storageInitializer: StorageInitializer
) {
    fun importDemoProject() {
        val project = Project.Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc")
        projectsRepository.save(project)
        storageInitializer.createProjectDirsOnStorage(project)
        currentProjectProvider.setCurrentProject(DEMO_PROJECT_ID)
    }

    // Now it does the same like importDemoProject() but it should be changed later
    fun importExistingProject() {
        val project = Project.Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc")
        projectsRepository.save(project)
        storageInitializer.createProjectDirsOnStorage(project)
        currentProjectProvider.setCurrentProject(DEMO_PROJECT_ID)
    }

    companion object {
        /*
        Should be empty to easily access existed settings (general and admin) that had be saved in versions
        prior to the one that implemented "Projects" and treat them as those which belong to
        the imported existed project.
         */
        const val DEMO_PROJECT_ID = "DEMO"
    }
}
