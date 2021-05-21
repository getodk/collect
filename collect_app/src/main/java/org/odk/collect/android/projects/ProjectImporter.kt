package org.odk.collect.android.projects

import org.odk.collect.android.storage.StorageInitializer
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class ProjectImporter(
    private val projectsRepository: ProjectsRepository,
    private val storageInitializer: StorageInitializer
) {
    fun importDemoProject() {
        val project = Project.Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc")
        projectsRepository.save(project)
        storageInitializer.createProjectDirsOnStorage(project)
    }

    fun importExistingProject(): Project.Saved {
        val project = projectsRepository.save(Project.New("Existing project", "E", "#3e9fcc"))
        storageInitializer.createProjectDirsOnStorage(project)
        return project
    }

    companion object {
        const val DEMO_PROJECT_ID = "DEMO"
    }
}
