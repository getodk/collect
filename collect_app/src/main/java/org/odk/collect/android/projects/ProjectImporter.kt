package org.odk.collect.android.projects

import org.odk.collect.projects.Project
import org.odk.collect.projects.Project.Saved
import org.odk.collect.projects.ProjectsRepository

class ProjectImporter(
    private val projectsRepository: ProjectsRepository
) {
    fun importNewProject(): Saved {
        return projectsRepository.save(Project.New("", "", ""))
    }

    fun importNewProject(project: Project): Saved {
        return projectsRepository.save(project)
    }
}
