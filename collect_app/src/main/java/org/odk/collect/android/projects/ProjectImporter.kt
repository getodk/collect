package org.odk.collect.android.projects

import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.projects.Project
import org.odk.collect.projects.Project.Saved
import org.odk.collect.projects.ProjectsRepository

class ProjectImporter(
    private val storagePathProvider: StoragePathProvider,
    private val projectsRepository: ProjectsRepository
) {
    fun importNewProject(): Saved {
        val savedProject = projectsRepository.save(Project.New("", "", ""))
        createProjectDirs(savedProject)
        return savedProject
    }

    fun importNewProject(project: Project): Saved {
        val savedProject = projectsRepository.save(project)
        createProjectDirs(savedProject)
        return savedProject
    }

    private fun createProjectDirs(project: Saved) {
        storagePathProvider.getProjectDirPaths(project.uuid).forEach { FileUtils.createDir(it) }
    }
}
